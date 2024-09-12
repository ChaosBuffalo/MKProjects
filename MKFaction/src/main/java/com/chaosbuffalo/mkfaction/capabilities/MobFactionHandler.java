package com.chaosbuffalo.mkfaction.capabilities;

import com.chaosbuffalo.mkfaction.event.MKFactionRegistry;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.chaosbuffalo.mkfaction.network.MobFactionAssignmentPacket;
import com.chaosbuffalo.targeting_api.Targeting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MobFactionHandler implements IMobFaction {
    private final LivingEntity entity;
    @Nullable
    private Holder<MKFaction> faction;

    public MobFactionHandler(LivingEntity entity) {
        this.entity = entity;
        this.faction = null;
    }

    @Nonnull
    @Override
    public LivingEntity getEntity() {
        return entity;
    }

    @Override
    public boolean hasFaction() {
        return faction != null;
    }

    @Nullable
    @Override
    public Holder<MKFaction> getFaction() {
        return faction;
    }

    @Override
    public ResourceLocation getFactionName() {
        if (faction != null) {
            return faction.unwrapKey().map(ResourceKey::location).orElse(MKFaction.INVALID_FACTION);
        }
        return MKFaction.INVALID_FACTION;
    }

    @Override
    public boolean isMember(MKFaction otherFaction) {
        if (faction != null) {
            return faction.value() == otherFaction;
        }
        return false;
    }

    @Override
    public ResourceLocation getBattlecryName() {
        return getFactionName().withPrefix("battlecry.");
    }

    @Override
    public void setFactionName(ResourceLocation factionName) {
        Holder<MKFaction> faction = MKFactionRegistry.getFactionHolder(entity.registryAccess(), factionName).orElse(null);

        setFaction(faction);
    }

    @Override
    public void setFaction(@Nullable Holder<MKFaction> faction) {
        this.faction = faction;
        if (!getEntity().getCommandSenderWorld().isClientSide) {
            syncToAllTracking();
        }
    }

    public void syncToAllTracking() {
        MobFactionAssignmentPacket updatePacket = new MobFactionAssignmentPacket(this);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(getEntity(), updatePacket);
    }

    @Override
    public Targeting.TargetRelation getRelationToEntity(LivingEntity otherEntity) {
        if (faction == null) {
            return Targeting.TargetRelation.UNHANDLED;
        }

        if (otherEntity instanceof Player player) {
            IPlayerFaction playerFaction = IPlayerFaction.getOrThrow(player);
            return playerFaction.getFactionRelation(this);
        }
        IMobFaction targetFaction = IMobFaction.getMobOrThrow(otherEntity);
        return faction.value().getNonPlayerEntityRelationship(otherEntity, targetFaction.getFaction());
    }

    @Override
    public CompoundTag serializeNBT(@Nonnull HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        if (faction != null) {
            var ops = provider.createSerializationContext(NbtOps.INSTANCE);
            tag.put("factionId", MKFaction.REFERENCE_CODEC.encodeStart(ops, faction).getOrThrow());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(@Nonnull HolderLookup.Provider provider, CompoundTag nbt) {
        if (nbt.contains("factionId")) {
            var ops = provider.createSerializationContext(NbtOps.INSTANCE);
            faction = MKFaction.REFERENCE_CODEC.parse(ops, nbt.get("factionId")).getOrThrow();
        }
    }
}
