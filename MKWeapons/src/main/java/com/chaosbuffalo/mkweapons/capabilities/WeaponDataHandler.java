package com.chaosbuffalo.mkweapons.capabilities;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkweapons.items.MKMeleeWeapon;
import com.chaosbuffalo.mkweapons.items.effects.ItemModifierEffect;
import com.chaosbuffalo.mkweapons.items.effects.melee.IMeleeWeaponEffect;
import com.chaosbuffalo.mkweapons.items.effects.ranged.IRangedWeaponEffect;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import com.chaosbuffalo.mkweapons.items.weapon.IMKRangedWeapon;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class WeaponDataHandler implements IWeaponData {

    private final ItemStack itemStack;
    private final List<IMeleeWeaponEffect> meleeWeaponEffects;
    private final List<IMeleeWeaponEffect> cachedMeleeWeaponEffects;
    private final List<IRangedWeaponEffect> rangedWeaponEffects;
    private final List<IRangedWeaponEffect> cachedRangedWeaponEffects;
    private final Map<EquipmentSlot, Multimap<Attribute, AttributeModifier>> modifiers = new EnumMap<>(EquipmentSlot.class);
    private ResourceLocation ability;
    private boolean isCacheDirty;

    public WeaponDataHandler(ItemStack itemStack) {
        this.itemStack = itemStack;
        meleeWeaponEffects = new ArrayList<>();
        cachedMeleeWeaponEffects = new ArrayList<>();
        rangedWeaponEffects = new ArrayList<>();
        cachedRangedWeaponEffects = new ArrayList<>();
        isCacheDirty = true;
        ability = MKCoreRegistry.INVALID_ABILITY;
    }

    @Override
    public ItemStack getItemStack() {
        return itemStack;
    }

    private List<IMeleeWeaponEffect> getStackMeleeEffects() {
        return meleeWeaponEffects;
    }

    @Override
    public List<IMeleeWeaponEffect> getMeleeEffects() {
        if (isCacheDirty) {
            cachedMeleeWeaponEffects.clear();
            if (getItemStack().getItem() instanceof IMKMeleeWeapon meleeWeapon) {
                cachedMeleeWeaponEffects.addAll(meleeWeapon.getWeaponEffects());
            }
            cachedMeleeWeaponEffects.addAll(getStackMeleeEffects());
            isCacheDirty = false;
        }
        return cachedMeleeWeaponEffects;
    }

    @Override
    public ResourceLocation getAbilityName() {
        return ability;
    }

    @Override
    public void setAbilityId(ResourceLocation ability) {
        this.ability = ability;
    }

    @Override
    public boolean hasMeleeWeaponEffects() {
        return !meleeWeaponEffects.isEmpty();
    }

    @Override
    public void markCacheDirty() {
        isCacheDirty = true;
        modifiers.clear();
    }

    @Override
    public void addMeleeWeaponEffect(IMeleeWeaponEffect weaponEffect) {
        meleeWeaponEffects.add(weaponEffect);
        markCacheDirty();
    }

    @Override
    public void removeMeleeWeaponEffect(int index) {
        meleeWeaponEffects.remove(index);
        markCacheDirty();
    }

    @Override
    public boolean hasRangedWeaponEffects() {
        return !rangedWeaponEffects.isEmpty();
    }

    @Override
    public void addRangedWeaponEffect(IRangedWeaponEffect weaponEffect) {
        rangedWeaponEffects.add(weaponEffect);
        markCacheDirty();
    }

    @Override
    public void removeRangedWeaponEffect(int index) {
        rangedWeaponEffects.remove(index);
        markCacheDirty();
    }

    private List<IRangedWeaponEffect> getStackRangedEffects() {
        return rangedWeaponEffects;
    }

    @Override
    public List<IRangedWeaponEffect> getRangedEffects() {
        if (isCacheDirty) {
            cachedRangedWeaponEffects.clear();
            if (getItemStack().getItem() instanceof IMKRangedWeapon rangedWeapon) {
                cachedRangedWeaponEffects.addAll(rangedWeapon.getWeaponEffects());
            }
            cachedRangedWeaponEffects.addAll(getStackRangedEffects());
            isCacheDirty = false;
        }
        return cachedRangedWeaponEffects;
    }

    private void loadSlotModifiers(EquipmentSlot slot) {
        Multimap<Attribute, AttributeModifier> modifiers = getItemStack().getItem().getDefaultAttributeModifiers(slot);
        Multimap<Attribute, AttributeModifier> newMods = HashMultimap.create();
        newMods.putAll(modifiers);
        if (slot == EquipmentSlot.MAINHAND) {
            if (itemStack.getItem() instanceof MKMeleeWeapon) {
                for (IMeleeWeaponEffect weaponEffect : getMeleeEffects()) {
                    if (weaponEffect instanceof ItemModifierEffect modEffect) {
                        modEffect.getModifiers().forEach(e -> newMods.put(e.getAttribute(), e.getModifier()));
                    }
                }
            } else {
                for (IRangedWeaponEffect weaponEffect : getRangedEffects()) {
                    if (weaponEffect instanceof ItemModifierEffect modEffect) {
                        modEffect.getModifiers().forEach(e -> newMods.put(e.getAttribute(), e.getModifier()));
                    }
                }
            }
        }
        this.modifiers.put(slot, newMods);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        if (!modifiers.containsKey(slot)) {
            loadSlotModifiers(slot);
        }
        return modifiers.get(slot);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        ListTag effectList = new ListTag();
        for (IMeleeWeaponEffect effect : getStackMeleeEffects()) {
            effectList.add(effect.serialize(NbtOps.INSTANCE));
        }
        nbt.put("melee_effects", effectList);
        ListTag rangedEffectList = new ListTag();
        for (IRangedWeaponEffect effect : getStackRangedEffects()) {
            rangedEffectList.add(effect.serialize(NbtOps.INSTANCE));
        }
        nbt.put("ranged_effects", rangedEffectList);
        ResourceLocation abilityId = getAbilityName() != null ? getAbilityName() : MKCoreRegistry.INVALID_ABILITY;
        nbt.putString("ability_granted", abilityId.toString());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("melee_effects")) {
            ListTag effectList = nbt.getList("melee_effects", Tag.TAG_COMPOUND);
            for (Tag effectNBT : effectList) {
                IMeleeWeaponEffect effect = IMeleeWeaponEffect.deserialize(new Dynamic<>(NbtOps.INSTANCE, effectNBT));
                if (effect != null) {
                    addMeleeWeaponEffect(effect);
                }
            }
        }
        if (nbt.contains("ranged_effects")) {
            ListTag rangedEffectList = nbt.getList("ranged_effects", Tag.TAG_COMPOUND);
            for (Tag effectNBT : rangedEffectList) {
                IRangedWeaponEffect effect = IRangedWeaponEffect.deserialize(new Dynamic<>(NbtOps.INSTANCE, effectNBT));
                if (effect != null) {
                    addRangedWeaponEffect(effect);
                }
            }
        }
        ResourceLocation abilityId = new ResourceLocation(nbt.getString("ability_granted"));
        if (!abilityId.equals(MKCoreRegistry.INVALID_ABILITY)) {
            setAbilityId(abilityId);
        }
    }
}
