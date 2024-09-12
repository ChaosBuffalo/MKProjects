package com.chaosbuffalo.mkfaction.capabilities;

import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.chaosbuffalo.mkfaction.init.FactionAttachments;
import com.chaosbuffalo.targeting_api.Targeting;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public interface IMobFaction extends INBTSerializable<CompoundTag> {

    @Nonnull
    LivingEntity getEntity();

    boolean hasFaction();

    ResourceLocation getFactionName();

    boolean isMember(MKFaction otherFaction);

    ResourceLocation getBattlecryName();

    void setFactionName(ResourceLocation factionName);

    void setFaction(@Nullable Holder<MKFaction> faction);

    @Nullable
    Holder<MKFaction> getFaction();

    Targeting.TargetRelation getRelationToEntity(LivingEntity entity);



    static Optional<IMobFaction> get(LivingEntity entity) {
        if (entity instanceof Player) {
            return Optional.empty();
        }
        return Optional.of(entity.getData(FactionAttachments.ENTITY_DATA_ATTACHMENT));
    }

    static Optional<IMobFaction> get(Entity entity) {
        if (entity instanceof LivingEntity living) {
            return get(living);
        }
        return Optional.empty();
    }

    static IMobFaction getMobOrThrow(LivingEntity entity) {
        return entity.getData(FactionAttachments.ENTITY_DATA_ATTACHMENT);
    }
}
