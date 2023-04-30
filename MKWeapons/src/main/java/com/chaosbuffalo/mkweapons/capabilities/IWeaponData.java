package com.chaosbuffalo.mkweapons.capabilities;

import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkweapons.items.effects.melee.IMeleeWeaponEffect;
import com.chaosbuffalo.mkweapons.items.effects.ranged.IRangedWeaponEffect;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.List;

public interface IWeaponData extends INBTSerializable<CompoundTag> {

    void attach(ItemStack itemStack);

    ItemStack getItemStack();

    List<IMeleeWeaponEffect> getMeleeEffects();

    ResourceLocation getAbilityName();

    @Nullable
    MKAbilityInfo getGrantedAbility();

    void setGrantedAbility(MKAbilityInfo abilityInfo);

    boolean hasMeleeWeaponEffects();

    void markCacheDirty();

    void addMeleeWeaponEffect(IMeleeWeaponEffect weaponEffect);

    void removeMeleeWeaponEffect(int index);

    boolean hasRangedWeaponEffects();

    void addRangedWeaponEffect(IRangedWeaponEffect weaponEffect);

    void removeRangedWeaponEffect(int index);

    List<IRangedWeaponEffect> getRangedEffects();

    Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot);
}
