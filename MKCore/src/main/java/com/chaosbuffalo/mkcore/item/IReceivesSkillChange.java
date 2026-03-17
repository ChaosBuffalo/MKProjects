package com.chaosbuffalo.mkcore.item;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IReceivesSkillChange {

    void onSkillChange(ItemStack itemStack, Player player, Holder<Attribute> skill);
}
