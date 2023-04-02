package com.chaosbuffalo.mkweapons.items.randomization.slots;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;

public class LootSlot {
    private final ResourceLocation name;
    private final BiConsumer<LivingEntity, ItemStack> slotSetter;

    public LootSlot(ResourceLocation name, BiConsumer<LivingEntity, ItemStack> slotSetter){
        this.name = name;
        this.slotSetter = slotSetter;
    }

    public LootSlot(ResourceLocation name, EquipmentSlot slotType){
        this(name, (entity, itemStack) -> entity.setItemSlot(slotType, itemStack));
    }


    public ResourceLocation getName() {
        return name;
    }

    public void setItemInSlot(LivingEntity entity, ItemStack item){
        slotSetter.accept(entity, item);
    }

}
