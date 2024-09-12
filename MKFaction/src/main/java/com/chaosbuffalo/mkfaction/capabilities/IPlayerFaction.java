package com.chaosbuffalo.mkfaction.capabilities;

import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.chaosbuffalo.mkfaction.faction.PlayerFactionEntry;
import com.chaosbuffalo.mkfaction.faction.PlayerFactionStatus;
import com.chaosbuffalo.mkfaction.init.FactionAttachments;
import com.chaosbuffalo.targeting_api.Targeting;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.Map;
import java.util.Optional;

public interface IPlayerFaction extends INBTSerializable<CompoundTag> {

    Player getPlayer();

    Map<ResourceLocation, PlayerFactionEntry> getFactionMap();

    Optional<PlayerFactionEntry> getFactionEntry(Holder<MKFaction> factionHolder);

    default PlayerFactionStatus getFactionStatus(IMobFaction mobFaction) {
        if (mobFaction.hasFaction()) {
            return getFactionStatus(mobFaction.getFaction());
        }
        return PlayerFactionStatus.UNKNOWN;
    }

    default PlayerFactionStatus getFactionStatus(Holder<MKFaction> factionHolder) {
        return getFactionEntry(factionHolder)
                .map(PlayerFactionEntry::getFactionStatus)
                .orElse(PlayerFactionStatus.UNKNOWN);
    }

    default Targeting.TargetRelation getFactionRelation(IMobFaction mobFaction) {
        if (mobFaction.hasFaction()) {
            return getFactionRelation(mobFaction.getFaction());
        }
        return Targeting.TargetRelation.UNHANDLED;
    }

    default Targeting.TargetRelation getFactionRelation(Holder<MKFaction> factionHolder) {
        return getFactionEntry(factionHolder)
                .map(PlayerFactionEntry::getTargetRelation)
                .orElse(Targeting.TargetRelation.UNHANDLED);
    }

    static Optional<IPlayerFaction> get(Player entity) {
        return Optional.of(entity.getData(FactionAttachments.PLAYER_DATA_ATTACHMENT));
    }

    static IPlayerFaction getOrThrow(Player entity) {
        return entity.getData(FactionAttachments.PLAYER_DATA_ATTACHMENT);
    }
}
