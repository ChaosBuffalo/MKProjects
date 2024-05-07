package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.IMKAbilityKnowledge;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.sync.adapters.SyncMapUpdater;
import com.chaosbuffalo.mkcore.sync.types.SyncInt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;


public class PlayerAbilityKnowledge implements IMKAbilityKnowledge, IPlayerSyncComponentProvider {
    private final MKPlayerData playerData;
    private final PlayerSyncComponent sync = new PlayerSyncComponent("abilities");
    private final Map<ResourceLocation, PlayerKnownAbility> knownAbilities = new HashMap<>();
    private final SyncInt poolSize = new SyncInt("poolSize", GameConstants.DEFAULT_ABILITY_POOL_SIZE);
    private final SyncMapUpdater<ResourceLocation, PlayerKnownAbility> knownAbilityUpdater =
            new SyncMapUpdater<>("known",
                    knownAbilities,
                    ResourceLocation::toString,
                    ResourceLocation::tryParse,
                    PlayerAbilityKnowledge::createKnownAbility
            );

    public PlayerAbilityKnowledge(MKPlayerData playerData) {
        this.playerData = playerData;
        addSyncPrivate(knownAbilityUpdater);
        addSyncPrivate(poolSize);
    }

    @Override
    public PlayerSyncComponent getSyncComponent() {
        return sync;
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

    public void setAbilityPoolSize(int count) {
        poolSize.set(Mth.clamp(count, GameConstants.DEFAULT_ABILITY_POOL_SIZE, GameConstants.MAX_ABILITY_POOL_SIZE));
    }

    private Stream<PlayerKnownAbility> getPoolAbilityStream() {
        return getKnownStream().filter(PlayerKnownAbility::usesAbilityPool);
    }

    public List<MKAbilityInfo> getPoolAbilities() {
        return getPoolAbilityStream().map(PlayerKnownAbility::getAbilityInfo).toList();
    }

    public int getCurrentPoolCount() {
        return (int) getPoolAbilityStream().count();
    }

    public boolean isAbilityPoolFull() {
        return getCurrentPoolCount() >= getAbilityPoolSize();
    }

    public Stream<PlayerKnownAbility> getKnownStream() {
        return knownAbilities.values().stream();
    }

    public Collection<PlayerKnownAbility> getKnownAbilities() {
        return Collections.unmodifiableCollection(knownAbilities.values());
    }

    @Override
    public Collection<MKAbilityInfo> getAllAbilities() {
        return getAbilityInfoStream().toList();
    }

    public Stream<MKAbilityInfo> getAbilityInfoStream() {
        return getKnownStream().map(PlayerKnownAbility::getAbilityInfo);
    }

    @Override
    public boolean learnAbility(MKAbility ability, AbilitySource source) {
        MKCore.LOGGER.debug("learnAbility {} {}", ability, source);
        PlayerKnownAbility knownAbility = getKnownAbility(ability.getAbilityId());
        if (knownAbility != null) {
            if (knownAbility.hasSource(source)) {
                // Already knows this ability from this source
                return true;
            }
            MKCore.LOGGER.warn("Player {} updated known ability {} with new source {}", playerData.getEntity(), knownAbility, source);
            knownAbility.addSource(source);
            markDirty(knownAbility);
            return true;
        }

        if (source.usesAbilityPool() && isAbilityPoolFull()) {
            MKCore.LOGGER.warn("Player {} tried to learn pool ability {} with a full pool ({}/{})",
                    playerData.getEntity(), ability.getAbilityId(), getCurrentPoolCount(), getAbilityPoolSize());
            return false;
        }

        knownAbility = knownAbilities.computeIfAbsent(ability.getAbilityId(), id -> new PlayerKnownAbility(ability.createAbilityInfo()));
        knownAbility.addSource(source);
        markDirty(knownAbility);

        playerData.events().trigger(PlayerEvents.ABILITY_LEARNED, new PlayerEvents.AbilityLearnEvent(playerData, knownAbility.getAbilityInfo(), source));
        return true;
    }

    @Override
    public boolean unlearnAbility(ResourceLocation abilityId, AbilitySource source) {
        PlayerKnownAbility knownAbility = getKnownAbility(abilityId);
        if (knownAbility == null) {
            MKCore.LOGGER.error("{} tried to unlearn unknown ability {}", playerData.getEntity(), abilityId);
            return false;
        }

        knownAbility.removeSource(source);
        markDirty(knownAbility);

        if (!knownAbility.isCurrentlyKnown()) {
            playerData.events().trigger(PlayerEvents.ABILITY_UNLEARNED, new PlayerEvents.AbilityUnlearnEvent(playerData, knownAbility.getAbilityInfo()));
            knownAbilities.remove(abilityId);
        }
        return true;
    }

    @Override
    public boolean knowsAbility(ResourceLocation abilityId) {
        return getKnownAbility(abilityId) != null;
    }

    @Nullable
    public MKAbilityInfo getAbilityInfo(ResourceLocation abilityId) {
        PlayerKnownAbility info = knownAbilities.get(abilityId);
        if (info == null)
            return null;
        return info.getAbilityInfo();
    }

    public PlayerKnownAbility getKnownAbility(ResourceLocation abilityId) {
        return knownAbilities.get(abilityId);
    }

    private void markDirty(PlayerKnownAbility knownAbility) {
        knownAbilityUpdater.markDirty(knownAbility.getId());
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

    private static PlayerKnownAbility createKnownAbility(ResourceLocation abilityId) {
        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability == null)
            return null;

        return new PlayerKnownAbility(ability.createAbilityInfo());
    }
}
