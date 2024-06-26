package com.chaosbuffalo.mkfaction.capabilities;

import com.chaosbuffalo.mkfaction.event.MKFactionRegistry;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.chaosbuffalo.mkfaction.network.MobFactionAssignmentPacket;
import com.chaosbuffalo.mkfaction.network.PacketHandler;
import com.chaosbuffalo.targeting_api.Targeting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;

public class MobFactionHandler implements IMobFaction {
    private final LivingEntity entity;
    private ResourceLocation factionName;
    private MKFaction faction;
    private ResourceLocation battlecryName;

    @Nullable
    private Component battlecry;

    public MobFactionHandler(LivingEntity entity) {
        this.entity = entity;
        setFactionNameInternal(MKFaction.INVALID_FACTION);
    }

    @Override
    public boolean hasFaction() {
        return faction != null;
    }

    @Nullable
    @Override
    public MKFaction getFaction() {
        return faction;
    }

    @Override
    public ResourceLocation getFactionName() {
        return factionName;
    }

    @Override
    public ResourceLocation getBattlecryName() {
        return battlecryName;
    }

    private void setFactionNameInternal(ResourceLocation factionName) {
        this.factionName = factionName;
        this.faction = MKFactionRegistry.getFaction(factionName);
        this.battlecryName = new ResourceLocation(factionName.getNamespace(), String.format("battlecry.%s", factionName.getPath()));
        if (!factionName.equals(MKFaction.INVALID_FACTION) && faction == null) {
            throw new IllegalStateException(String.format("Entity %s was switched to unregistered faction '%s'", entity, factionName));
        }
    }

    public void setFactionName(ResourceLocation factionName) {
        setFactionNameInternal(factionName);
        if (!getEntity().getCommandSenderWorld().isClientSide) {
            syncToAllTracking();
        }
    }

    public void syncToAllTracking() {
        MobFactionAssignmentPacket updatePacket = new MobFactionAssignmentPacket(this);
        PacketDistributor.TRACKING_ENTITY.with(this::getEntity)
                .send(PacketHandler.getNetworkChannel().toVanillaPacket(updatePacket, NetworkDirection.PLAY_TO_CLIENT));
    }

    @Override
    public LivingEntity getEntity() {
        return entity;
    }

    @Override
    public Targeting.TargetRelation getRelationToEntity(LivingEntity otherEntity) {
        MKFaction faction = getFaction();
        if (faction == null) {
            return Targeting.TargetRelation.UNHANDLED;
        }

        if (otherEntity instanceof Player) {
            return otherEntity.getCapability(FactionCapabilities.PLAYER_FACTION_CAPABILITY)
                    .map(playerFaction -> playerFaction.getFactionRelation(factionName))
                    .orElse(Targeting.TargetRelation.UNHANDLED);
        }
        return otherEntity.getCapability(FactionCapabilities.MOB_FACTION_CAPABILITY)
                .map(mobFaction -> faction.getNonPlayerEntityRelationship(otherEntity, mobFaction.getFactionName(), mobFaction.getFaction()))
                .orElse(Targeting.TargetRelation.UNHANDLED);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("factionName", getFactionName().toString());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("factionName")) {
            setFactionNameInternal(new ResourceLocation(nbt.getString("factionName")));
        } else {
            setFactionNameInternal(MKFaction.INVALID_FACTION);
        }
    }
}
