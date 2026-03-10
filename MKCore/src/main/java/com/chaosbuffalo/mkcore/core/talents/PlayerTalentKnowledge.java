package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.MKConfig;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.core.records.PlayerRecordDispatcher;
import com.chaosbuffalo.mkcore.init.CoreSounds;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import com.chaosbuffalo.mkcore.sync.types.SyncInt;
import com.chaosbuffalo.mkcore.sync.v2.ISyncGroupProvider;
import com.chaosbuffalo.mkcore.sync.v2.ISyncObject;
import com.chaosbuffalo.mkcore.sync.v2.SyncGroup;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlayerTalentKnowledge implements ISyncGroupProvider {
    private final MKPlayerData playerData;
    private final SyncGroup syncGroup = new SyncGroup();
    private final SyncInt talentPoints = new SyncInt(0);
    private final SyncInt totalTalentPoints = new SyncInt(0);
    private final Map<ResourceLocation, TalentTreeRecord> talentTreeRecordMap = new HashMap<>();
    private final SyncInt talentXp = new SyncInt(0);
    private final PlayerRecordDispatcher<TalentRecord> dispatcher;
    private final TreeSyncGroup treeGroup;

    public PlayerTalentKnowledge(Persona persona) {
        this.playerData = persona.getPlayerData();
        dispatcher = new PlayerRecordDispatcher<>(persona, this::getKnownTalentsStream);
        syncGroup.addPrivate("points", talentPoints);
        syncGroup.addPrivate("totalPoints", totalTalentPoints);
        syncGroup.addPrivate("xp", talentXp);
        treeGroup = new TreeSyncGroup();
        syncGroup.addChild("trees", treeGroup);
        unlockDefaultTrees();
    }

    public int getTalentXp() {
        return talentXp.get();
    }

    public int getXpToNextLevel() {
        return MKConfig.SERVER.baseXpPerTalentPoint.get() +
                Math.round((getTotalTalentPoints() * MKConfig.SERVER.totalTalentXpMultiplier.get().floatValue()) *
                        MKConfig.SERVER.scalingXpPerTalentPoint.get());
    }

    public float getXpProgressPercent() {
        int currentXp = getTalentXp();
        int nextLevel = getXpToNextLevel();
        return (float) currentXp / (float) nextLevel;
    }

    public boolean shouldLevel() {
        return getTalentXp() >= getXpToNextLevel();
    }

    public void addTalentXp(int value) {
        int maxPoints = MKConfig.SERVER.maxTalentPoints.get();
        if (maxPoints > 0 && getTotalTalentPoints() >= MKConfig.SERVER.maxTalentPoints.get()) {
            return;
        }
        talentXp.add(value);
        if (shouldLevel()) {
            performLevel();
        }
    }

    private void performLevel() {
        if (playerData.isServerSide()) {
            talentXp.add(-getXpToNextLevel());
            grantTalentPoints(1);
            SoundUtils.serverPlaySoundAtEntity(playerData.getEntity(), CoreSounds.level_up.value(), SoundSource.PLAYERS);
            playerData.getStats().setHealth(playerData.getStats().getMaxHealth());
            playerData.getStats().setMana(playerData.getStats().getMaxMana());
        }
    }

    @Override
    public SyncGroup getSyncGroup() {
        return syncGroup;
    }

    public int getTotalTalentPoints() {
        return totalTalentPoints.get();
    }

    public int getUnspentTalentPoints() {
        return talentPoints.get();
    }

    public Stream<TalentRecord> getKnownTalentsStream() {
        return talentTreeRecordMap.values()
                .stream()
                .flatMap(TalentTreeRecord::getRecordStream)
                .filter(TalentRecord::isKnown);
    }

    public Collection<ResourceLocation> getKnownTreeNames() {
        return Collections.unmodifiableCollection(talentTreeRecordMap.keySet());
    }

    public Collection<TalentTreeRecord> getKnownTrees() {
        return Collections.unmodifiableCollection(talentTreeRecordMap.values());
    }

    public boolean unlockTree(ResourceKey<TalentTreeDefinition> treeId) {
        var record = unlockTreeInternal(treeId.location());
        if (record == null) {
            return false;
        }
        treeGroup.addTree(record, true);
        return true;
    }

    @Nullable
    protected TalentTreeRecord unlockTreeInternal(ResourceLocation treeId) {
        var existing = talentTreeRecordMap.get(treeId);
        if (existing != null) {
            MKCore.LOGGER.warn("Player {} tried to unlock already-known talent tree {}", playerData.getEntity(), treeId);
            return existing;
        }

        TalentTreeDefinition tree = TalentManager.getTalentTree(playerData.getEntity().registryAccess(), treeId);
        if (tree == null) {
            MKCore.LOGGER.warn("Player {} tried to unlock unknown tree {}", playerData.getEntity(), treeId);
            return null;
        }

        TalentTreeRecord record = tree.createRecord(treeId);
        if (record == null) {
            return null;
        }
        talentTreeRecordMap.put(treeId, record);
        return record;
    }

    private void unlockDefaultTrees() {
        if (playerData.isClientSide())
            return;

        for (ResourceKey<TalentTreeDefinition> treeId : TalentManager.getDefaultTrees(playerData.getEntity().registryAccess())) {
            if (!unlockTree(treeId)) {
                MKCore.LOGGER.error("Failed to unlock default talent tree: {}", treeId);
            }
        }
    }

    public boolean knowsTree(ResourceKey<TalentTreeDefinition> treeId) {
        return talentTreeRecordMap.containsKey(treeId.location());
    }

    public TalentTreeRecord getTree(ResourceKey<TalentTreeDefinition> treeId) {
        return talentTreeRecordMap.get(treeId.location());
    }

    public TalentRecord getRecord(ResourceKey<TalentTreeDefinition> treeId, String line, int index) {
        TalentTreeRecord treeRecord = getTree(treeId);
        if (treeRecord == null)
            return null;

        return treeRecord.getNodeRecord(line, index);
    }

    public boolean grantTalentPoints(int amount) {
        if (amount > 0) {
            talentPoints.add(amount);
            totalTalentPoints.add(amount);
            return true;
        }
        return false;
    }

    public boolean removeTalentPoints(int amount) {
        if (amount > 0 && amount <= talentPoints.get() && amount <= totalTalentPoints.get()) {
            talentPoints.add(-amount);
            totalTalentPoints.add(-amount);
            return true;
        }

        return false;
    }

    public boolean spendTalentPoint(ResourceKey<TalentTreeDefinition> treeId, String line, int index) {
        if (getUnspentTalentPoints() == 0) {
            MKCore.LOGGER.warn("Player {} attempted to spend talent ({}, {}) - no unspent points", playerData.getEntity(), treeId.location(), line);
            return false;
        }

        TalentTreeRecord treeRecord = getTree(treeId);
        if (treeRecord == null) {
            MKCore.LOGGER.warn("Player {} attempted to spend talent ({}, {}) - tree not known", playerData.getEntity(), treeId.location(), line);
            return false;
        }

        if (!treeRecord.trySpendPoint(line, index)) {
            MKCore.LOGGER.warn("Player {} attempted to spend talent ({}, {}) - requirement not met", playerData.getEntity(), treeId.location(), line);
            return false;
        }

        talentPoints.add(-1);

        TalentRecord record = treeRecord.getNodeRecord(line, index);
        if (record != null) {
            dispatcher.onRecordUpdated(record);
        }
        return true;
    }

    public boolean refundTalentPoint(ResourceKey<TalentTreeDefinition> treeId, String line, int index) {
        TalentTreeRecord treeRecord = getTree(treeId);
        if (treeRecord == null) {
            MKCore.LOGGER.warn("Player {} attempted to unlearn talent in unknown tree {}", playerData.getEntity(), treeId.location());
            return false;
        }

        if (!treeRecord.tryRefundPoint(line, index)) {
            MKCore.LOGGER.warn("Player {} attempted to refund talent ({}, {}) - requirement not met", playerData.getEntity(), treeId.location(), line);
            return false;
        }

        talentPoints.add(1);

        TalentRecord record = treeRecord.getNodeRecord(line, index);
        if (record != null) {
            dispatcher.onRecordUpdated(record);
        }
        return true;
    }

    public <T> T serialize(DynamicOps<T> ops) {
        ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
        builder.put(ops.createString("talentXp"), ops.createInt(talentXp.get()));
        builder.put(ops.createString("totalPoints"), ops.createInt(totalTalentPoints.get()));
        builder.put(ops.createString("trees"), ops.createMap(talentTreeRecordMap.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                kv -> ops.createString(kv.getKey().toString()),
                                kv -> kv.getValue().serialize(ops)
                        )
                )));

        return ops.createMap(builder.build());
    }

    public <T> void deserialize(Dynamic<T> dynamic) {
        talentXp.set(dynamic.get("talentXp").asInt(0));
        totalTalentPoints.set(dynamic.get("totalPoints").asInt(0));
        talentPoints.set(totalTalentPoints.get());

        dynamic.get("trees")
                .asMap(d -> ResourceLocation.CODEC.parse(d).getOrThrow(), Function.identity())
                .forEach(this::deserializeTree);
    }

    private <T> void deserializeTree(ResourceLocation treeId, Dynamic<T> dyn) {
        TalentTreeDefinition tree = TalentManager.getTalentTree(playerData.getEntity().registryAccess(), treeId);
        if (tree == null) {
            MKCore.LOGGER.warn("Player {} tried to unlock unknown tree {}", playerData.getEntity(), treeId);
            return;
        }

        TalentTreeRecord treeRecord = tree.createRecord(treeId);
        if (!treeRecord.deserialize(dyn)) {
            MKCore.LOGGER.error("Player {} had invalid talent layout for tree {}. Points will be refunded.", playerData.getEntity(), treeId);
        } else {
            // If the tree deserializes properly subtract the points spent in it from the total points
            talentPoints.add(-treeRecord.getPointsSpent());

            talentTreeRecordMap.put(treeId, treeRecord);
            treeGroup.addTree(treeRecord, false);
        }
    }

    public Tag serializeNBT(HolderLookup.Provider provider) {
        return serialize(provider.createSerializationContext(NbtOps.INSTANCE));
    }

    public void deserializeNBT(HolderLookup.Provider provider, Tag tag) {
        var ops = provider.createSerializationContext(NbtOps.INSTANCE);
        deserialize(new Dynamic<>(ops, tag));
    }

    public void onPersonaActivated() {
        dispatcher.onPersonaActivated();
    }

    public void onPersonaDeactivated() {
        dispatcher.onPersonaDeactivated();
    }

    class TreeSyncGroup extends SyncGroup {
        public TreeSyncGroup() {
            setDynamicMemberFactory(this::handleNewTreeRecord);
        }

        private ISyncObject handleNewTreeRecord(String name, Tag tag, SyncVisibility visibility) {
            ResourceLocation treeId = ResourceLocation.tryParse(name);
            if (treeId == null)
                return null;

            var record = unlockTreeInternal(treeId);
            return record != null ? record.getUpdater() : null;
        }

        void addTree(TalentTreeRecord treeRecord, boolean sendUpdate) {
            var treeId = treeRecord.getTreeId().location();
            add(treeId.toString(), treeRecord.getUpdater(), SyncVisibility.Private, sendUpdate);
        }
    }
}
