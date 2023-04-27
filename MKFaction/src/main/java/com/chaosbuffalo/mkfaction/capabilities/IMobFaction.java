package com.chaosbuffalo.mkfaction.capabilities;

import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.chaosbuffalo.targeting_api.Targeting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Optional;

public interface IMobFaction extends INBTSerializable<CompoundTag> {

    boolean hasFaction();

    ResourceLocation getFactionName();

    ResourceLocation getBattlecryName();

    void setFactionName(ResourceLocation factionName);

    MKFaction getFaction();

    Targeting.TargetRelation getRelationToEntity(LivingEntity entity);

    LivingEntity getEntity();
}
