package com.chaosbuffalo.mkfaction.capabilities;

import com.chaosbuffalo.mkfaction.entities.IEntitySpawnIdentity;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.chaosbuffalo.mkfaction.faction.PlayerFactionEntry;
import com.chaosbuffalo.mkfaction.faction.PlayerFactionStatus;
import com.chaosbuffalo.mkfaction.init.FactionAttachments;
import com.chaosbuffalo.targeting_api.Targeting;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

import javax.annotation.Nullable;

public interface IPlayerFaction extends INBTSerializable<CompoundTag> {

    Player getPlayer();

    Map<Holder<MKFaction>, PlayerFactionEntry> getFactionMap();

    Optional<PlayerFactionEntry> getFactionEntry(Holder<MKFaction> factionHolder);

    OptionalInt getNpcFactionOverride(UUID spawnId);

    void setNpcFactionOverride(UUID spawnId, int factionScore);

    void clearNpcFactionOverride(UUID spawnId);

    default int getFactionScore(Holder<MKFaction> factionHolder) {
        return getFactionEntry(factionHolder)
                .map(PlayerFactionEntry::getFactionScore)
                .orElse(factionHolder.value().getDefaultPlayerScore());
    }

    default int getFactionScore(Holder<MKFaction> factionHolder, @Nullable UUID spawnId) {
        if (spawnId != null) {
            OptionalInt override = getNpcFactionOverride(spawnId);
            if (override.isPresent()) {
                return override.getAsInt();
            }
        }
        return getFactionScore(factionHolder);
    }

    default int getFactionScore(LivingEntity target) {
        return IMobFaction.get(target)
                .map(mobFaction -> {
                    Holder<MKFaction> mobActiveFaction = mobFaction.getFaction();
                    if (mobActiveFaction == null) {
                        return 0;
                    }
                    return getFactionScore(mobActiveFaction, IEntitySpawnIdentity.get(target).map(IEntitySpawnIdentity::getSpawnID).orElse(null));
                })
                .orElse(0);
    }

    default PlayerFactionStatus getFactionStatus(IMobFaction mobFaction) {
        return getFactionStatus(mobFaction.getEntity());
    }

    default PlayerFactionStatus getFactionStatus(LivingEntity target) {
        return IMobFaction.get(target)
                .map(mobFaction -> {
                    Holder<MKFaction> mobActiveFaction = mobFaction.getFaction();
                    if (mobActiveFaction == null) {
                        return PlayerFactionStatus.UNKNOWN;
                    }
                    return getFactionStatus(mobActiveFaction, IEntitySpawnIdentity.get(target).map(IEntitySpawnIdentity::getSpawnID).orElse(null));
                })
                .orElse(PlayerFactionStatus.UNKNOWN);
    }

    default PlayerFactionStatus getFactionStatus(Holder<MKFaction> factionHolder) {
        return getFactionStatus(factionHolder, null);
    }

    default PlayerFactionStatus getFactionStatus(Holder<MKFaction> factionHolder, @Nullable UUID spawnId) {
        return PlayerFactionStatus.forScore(getFactionScore(factionHolder, spawnId));
    }

    default Targeting.TargetRelation getFactionRelation(IMobFaction mobFaction) {
        return getFactionRelation(mobFaction.getEntity());
    }

    default Targeting.TargetRelation getFactionRelation(LivingEntity target) {
        return IMobFaction.get(target)
                .map(mobFaction -> {
                    Holder<MKFaction> mobActiveFaction = mobFaction.getFaction();
                    if (mobActiveFaction == null) {
                        return Targeting.TargetRelation.UNHANDLED;
                    }
                    return getFactionRelation(mobActiveFaction, IEntitySpawnIdentity.get(target).map(IEntitySpawnIdentity::getSpawnID).orElse(null));
                })
                .orElse(Targeting.TargetRelation.UNHANDLED);
    }

    default Targeting.TargetRelation getFactionRelation(Holder<MKFaction> factionHolder) {
        return getFactionRelation(factionHolder, null);
    }

    default Targeting.TargetRelation getFactionRelation(Holder<MKFaction> factionHolder, @Nullable UUID spawnId) {
        return getFactionStatus(factionHolder, spawnId).getRelation();
    }

    static Optional<IPlayerFaction> get(Player entity) {
        return Optional.of(entity.getData(FactionAttachments.PLAYER_DATA_ATTACHMENT));
    }

    static IPlayerFaction getOrThrow(Player entity) {
        return entity.getData(FactionAttachments.PLAYER_DATA_ATTACHMENT);
    }
}
