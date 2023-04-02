package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.IMKAbilityKnowledge;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.sync.SyncInt;
import com.chaosbuffalo.mkcore.sync.SyncMapUpdater;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class PlayerAbilityKnowledge implements IMKAbilityKnowledge, IPlayerSyncComponentProvider {
    private final MKPlayerData playerData;
    private final SyncComponent sync = new SyncComponent("abilities");
    private final Map<ResourceLocation, MKAbilityInfo> abilityInfoMap = new HashMap<>();
    private final SyncInt poolSize = new SyncInt("poolSize", GameConstants.DEFAULT_ABILITY_POOL_SIZE);
    private final SyncMapUpdater<ResourceLocation, MKAbilityInfo> knownAbilityUpdater =
            new SyncMapUpdater<>("known",
                    () -> abilityInfoMap,
                    ResourceLocation::toString,
                    ResourceLocation::tryParse,
                    PlayerAbilityKnowledge::createAbilityInfo
            );

    public PlayerAbilityKnowledge(MKPlayerData playerData) {
        this.playerData = playerData;
        addSyncPrivate(knownAbilityUpdater);
        addSyncPrivate(poolSize);
    }

    public int getAbilityPoolSize() {
        return poolSize.get();
    }

    public int getEmptySlotCount() {
        return getAbilityPoolSize() - getCurrentPoolCount();
    }

    public int getSlotDeficitToLearnAnAbility() {
        int emptySlots = getEmptySlotCount();
        if (emptySlots <= 0) {
            return 1 - emptySlots;
        }
        return 0;
    }

    public void modifyAbilityPoolSize(int delta) {
        poolSize.add(delta);
    }

    public void setAbilityPoolSize(int count) {
        poolSize.set(Mth.clamp(count, GameConstants.DEFAULT_ABILITY_POOL_SIZE, GameConstants.MAX_ABILITY_POOL_SIZE));
    }

    private Stream<ResourceLocation> getPoolAbilityStream() {
        // This can be cached easily if it ever becomes a problem
        return getKnownStream()
                .filter(MKAbilityInfo::usesAbilityPool)
                .map(MKAbilityInfo::getId);
    }

    public List<ResourceLocation> getPoolAbilities() {
        return getPoolAbilityStream().collect(Collectors.toList());
    }

    public int getCurrentPoolCount() {
        return (int) getPoolAbilityStream().count();
    }

    public boolean isAbilityPoolFull() {
        return getCurrentPoolCount() >= getAbilityPoolSize();
    }

    @Override
    public SyncComponent getSyncComponent() {
        return sync;
    }

    @Override
    public Collection<MKAbilityInfo> getAllAbilities() {
        return Collections.unmodifiableCollection(abilityInfoMap.values());
    }

    public Stream<MKAbilityInfo> getKnownStream() {
        return abilityInfoMap.values().stream().filter(MKAbilityInfo::isCurrentlyKnown);
    }

    private AbilityGroup getAbilityGroup(MKAbility ability) {
        return playerData.getLoadout().getAbilityGroup(ability.getType().getGroup());
    }

    @Override
    public boolean learnAbility(MKAbility ability, AbilitySource source) {
        MKCore.LOGGER.debug("learnAbility {} {}", ability, source);
        MKAbilityInfo info = getKnownAbility(ability.getAbilityId());
        if (info != null) {
            if (info.hasSource(source)) {
                // Already knows this ability from this source
                return true;
            }
            MKCore.LOGGER.warn("Player {} updated known ability {} with new source {}", playerData.getEntity(), info, source);
            info.addSource(source);
            markDirty(info);
            return true;
        }

        if (source.usesAbilityPool() && isAbilityPoolFull()) {
            MKCore.LOGGER.warn("Player {} tried to learn pool ability {} with a full pool ({}/{})",
                    playerData.getEntity(), ability.getAbilityId(), getCurrentPoolCount(), getAbilityPoolSize());
            return false;
        }

        info = abilityInfoMap.computeIfAbsent(ability.getAbilityId(), id -> ability.createAbilityInfo());
        info.addSource(source);
        markDirty(info);

        AbilityGroup group = getAbilityGroup(ability);
        group.onAbilityLearned(info);
        if (source.placeOnBarWhenLearned()) {
            group.tryEquip(ability.getAbilityId());
        }
        return true;
    }

    @Override
    public boolean unlearnAbility(ResourceLocation abilityId, AbilitySource source) {
        MKAbilityInfo info = getKnownAbility(abilityId);
        if (info == null) {
            MKCore.LOGGER.error("{} tried to unlearn unknown ability {}", playerData.getEntity(), abilityId);
            return false;
        }

        info.removeSource(source);
        markDirty(info);

        if (!info.isCurrentlyKnown()) {
            MKAbility ability = info.getAbility();
            playerData.getAbilityExecutor().onAbilityUnlearned(ability);
            getAbilityGroup(ability).onAbilityUnlearned(info);
            abilityInfoMap.remove(abilityId);
        }
        return true;
    }

    @Override
    public boolean knowsAbility(ResourceLocation abilityId) {
        return getKnownAbility(abilityId) != null;
    }

    public boolean knowsAbility(ResourceLocation abilityId, AbilitySource source) {
        MKAbilityInfo info = getKnownAbility(abilityId);
        return info != null && info.hasSource(source);
    }

    @Nullable
    public MKAbilityInfo getKnownAbility(ResourceLocation abilityId) {
        MKAbilityInfo info = abilityInfoMap.get(abilityId);
        if (info == null || !info.isCurrentlyKnown())
            return null;
        return info;
    }

    private void markDirty(MKAbilityInfo info) {
        knownAbilityUpdater.markDirty(info.getId());
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.put("known", knownAbilityUpdater.serializeStorage());
        tag.putInt("poolSize", poolSize.get());
        return tag;
    }

    public void deserialize(CompoundTag tag) {
        knownAbilityUpdater.deserializeStorage(tag.get("known"));
        setAbilityPoolSize(tag.getInt("poolSize"));
    }

    private static MKAbilityInfo createAbilityInfo(ResourceLocation abilityId) {
        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability == null)
            return null;

        return ability.createAbilityInfo();
    }
}
