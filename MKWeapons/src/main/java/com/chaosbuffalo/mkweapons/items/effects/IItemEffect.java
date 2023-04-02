package com.chaosbuffalo.mkweapons.items.effects;

import com.chaosbuffalo.mkcore.serialization.IDynamicMapTypedSerializer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public interface IItemEffect extends IDynamicMapTypedSerializer {

    void addInformation(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip);

    default void onEntityEquip(LivingEntity entity) {}

    default void onEntityUnequip(LivingEntity entity) {}

    default void onSkillChange(Player player) {};
}
