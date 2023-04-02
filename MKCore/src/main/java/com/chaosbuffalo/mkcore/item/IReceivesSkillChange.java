package com.chaosbuffalo.mkcore.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IReceivesSkillChange {

    void onSkillChange(ItemStack itemStack, Player player);
}
