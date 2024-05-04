package com.chaosbuffalo.mkweapons.capabilities;

import com.chaosbuffalo.mkweapons.items.accessories.MKAccessory;
import com.chaosbuffalo.mkweapons.items.effects.ItemModifierEffect;
import com.chaosbuffalo.mkweapons.items.effects.accesory.IAccessoryEffect;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import java.util.*;

public class MKCurioItemHandler implements ICurio, INBTSerializable<CompoundTag> {

    private final ItemStack stack;
    private final List<IAccessoryEffect> effects;
    private final List<IAccessoryEffect> cachedEffects;
    private final Map<String, Multimap<Attribute, AttributeModifier>> modifiers = new HashMap<>();
    private boolean isCacheDirty;

    public MKCurioItemHandler(ItemStack itemStack) {
        this.stack = itemStack;
        effects = new ArrayList<>();
        cachedEffects = new ArrayList<>();
        isCacheDirty = true;
    }

    private List<IAccessoryEffect> getStackEffects() {
        return effects;
    }

    @Override
    public ItemStack getStack() {
        return stack;
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack) {
        for (IAccessoryEffect effect : getEffects()) {
            effect.onEntityEquip(slotContext.entity());
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack) {
        for (IAccessoryEffect effect : getEffects()) {
            effect.onEntityUnequip(slotContext.entity());
        }
    }

    public void addEffect(IAccessoryEffect effect) {
        effects.add(effect);
        markCacheDirty();
    }

    public void markCacheDirty() {
        isCacheDirty = true;
        modifiers.clear();
    }

    public void removeEffect(int index) {
        effects.remove(index);
        markCacheDirty();
    }

    public MKAccessory getAccessory() {
        return (MKAccessory) getStack().getItem();
    }

    public List<IAccessoryEffect> getEffects() {
        if (isCacheDirty) {
            cachedEffects.clear();
            if (getStack().getItem() instanceof MKAccessory accessory) {
                cachedEffects.addAll(accessory.getAccessoryEffects());
            }
            cachedEffects.addAll(getStackEffects());
            isCacheDirty = false;
        }
        return cachedEffects;
    }

    public boolean hasEffects() {
        return !effects.isEmpty();
    }

    private void loadSlotModifiers(String slotId) {
        Multimap<Attribute, AttributeModifier> newMods = HashMultimap.create();
        for (IAccessoryEffect weaponEffect : getEffects()) {
            if (weaponEffect instanceof ItemModifierEffect modEffect) {
                modEffect.getModifiers().forEach(e -> newMods.put(e.getAttribute(), e.getModifier()));
            }
        }
        this.modifiers.put(slotId, newMods);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid) {
        if (!modifiers.containsKey(slotContext.identifier())) {
            loadSlotModifiers(slotContext.identifier());
        }
        return modifiers.get(slotContext.identifier());
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        ListTag effectList = new ListTag();
        for (IAccessoryEffect effect : getStackEffects()) {
            effectList.add(effect.serialize(NbtOps.INSTANCE));
        }
        nbt.put("accessory_effects", effectList);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("accessory_effects")) {
            ListTag effectList = nbt.getList("accessory_effects", Tag.TAG_COMPOUND);
            effects.clear();
            for (Tag effectNBT : effectList) {
                IAccessoryEffect effect = IAccessoryEffect.deserialize(new Dynamic<>(NbtOps.INSTANCE, effectNBT));
                if (effect != null) {
                    addEffect(effect);
                }
            }
        }
    }
}
