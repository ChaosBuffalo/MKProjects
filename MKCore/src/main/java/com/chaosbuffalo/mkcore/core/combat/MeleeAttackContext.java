package com.chaosbuffalo.mkcore.core.combat;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record MeleeAttackContext(Player player,
                                 Entity target,
                                 InteractionHand hand,
                                 ItemStack weaponStack,
                                 int requiredAttackStrengthTicks,
                                 int swingDurationTicks) {
}
