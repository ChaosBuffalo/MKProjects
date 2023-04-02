package com.chaosbuffalo.mkfaction.capabilities;

import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.chaosbuffalo.targeting_api.Targeting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

public interface IMobFaction extends INBTSerializable<CompoundTag> {

    boolean hasFaction();

    ResourceLocation getFactionName();

    void setFactionName(ResourceLocation factionName);

    MKFaction getFaction();

    Targeting.TargetRelation getRelationToEntity(LivingEntity entity);

    LivingEntity getEntity();

}
