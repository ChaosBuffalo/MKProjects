package com.chaosbuffalo.mkweapons.items.effects;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public interface IItemEffect {

    ResourceLocation getTypeName();

    void addInformation(ItemStack stack, @Nullable Player player, List<Component> tooltip);

    default void onEntityEquip(LivingEntity entity) {
    }

    default void onEntityUnequip(LivingEntity entity) {
    }

    default void onSkillChange(Player player) {
    }
}
