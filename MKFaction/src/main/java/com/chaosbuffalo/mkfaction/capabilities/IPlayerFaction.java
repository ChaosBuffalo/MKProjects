package com.chaosbuffalo.mkfaction.capabilities;

import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.chaosbuffalo.mkfaction.faction.PlayerFactionEntry;
import com.chaosbuffalo.mkfaction.faction.PlayerFactionStatus;
import com.chaosbuffalo.targeting_api.Targeting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Map;
import java.util.Optional;

public interface IPlayerFaction extends INBTSerializable<CompoundTag> {

    Player getPlayer();

    Map<ResourceLocation, PlayerFactionEntry> getFactionMap();

    Optional<PlayerFactionEntry> getFactionEntry(ResourceLocation factionName);

    default Optional<PlayerFactionEntry> getFactionEntry(MKFaction faction) {
        return getFactionEntry(faction.getId());
    }

    default PlayerFactionStatus getFactionStatus(ResourceLocation factionName) {
        return getFactionEntry(factionName)
                .map(PlayerFactionEntry::getFactionStatus)
                .orElse(PlayerFactionStatus.UNKNOWN);
    }

    default PlayerFactionStatus getFactionStatus(IMobFaction mobFaction) {
        return getFactionStatus(mobFaction.getFactionName());
    }

    default Targeting.TargetRelation getFactionRelation(ResourceLocation factionName) {
        return getFactionEntry(factionName)
                .map(PlayerFactionEntry::getTargetRelation)
                .orElse(Targeting.TargetRelation.UNHANDLED);
    }

    default Targeting.TargetRelation getFactionRelation(IMobFaction mobFaction) {
        return getFactionRelation(mobFaction.getFactionName());
    }

    default void addRepToFaction(ResourceLocation factionName, int factionAmount) {
        getFactionEntry(factionName).ifPresent(entry -> entry.incrementFaction(factionAmount));
    }

    default void subRepFromFaction(ResourceLocation factionName, int factionAmount) {
        getFactionEntry(factionName).ifPresent(entry -> entry.decrementFaction(factionAmount));
    }
}
