package com.chaosbuffalo.mkcore.core.player;

import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

public final class PlayerMeleeHandStatsResolver {
    private PlayerMeleeHandStatsResolver() {
    }

    public static int getRequiredAttackStrengthTicks(PlayerCombatExtensionModule combat, InteractionHand hand) {
        return Math.max(1, Mth.ceil(resolveAttackSpeedDelay(combat, hand)));
    }

    public static float resolveAttackDamage(PlayerCombatExtensionModule combat, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            return (float) combat.getPlayerData().getEntity().getAttributeValue(Attributes.ATTACK_DAMAGE);
        }
        double currentAttackDamage = combat.getPlayerData().getEntity().getAttributeValue(Attributes.ATTACK_DAMAGE);
        double mainHandAttackDamage = getItemAddValueModifier(combat.getPlayerData().getEntity().getMainHandItem(), Attributes.ATTACK_DAMAGE);
        double selectedHandAttackDamage = getItemAddValueModifier(combat.getPlayerData().getEntity().getItemInHand(hand), Attributes.ATTACK_DAMAGE);
        return (float) (currentAttackDamage - mainHandAttackDamage + selectedHandAttackDamage);
    }

    public static float resolveAttackKnockback(PlayerCombatExtensionModule combat, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            return (float) combat.getPlayerData().getEntity().getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        }
        double currentAttackKnockback = combat.getPlayerData().getEntity().getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        double mainHandAttackKnockback = getItemAddValueModifier(combat.getPlayerData().getEntity().getMainHandItem(), Attributes.ATTACK_KNOCKBACK);
        double selectedHandAttackKnockback = getItemAddValueModifier(combat.getPlayerData().getEntity().getItemInHand(hand), Attributes.ATTACK_KNOCKBACK);
        return (float) (currentAttackKnockback - mainHandAttackKnockback + selectedHandAttackKnockback);
    }

    public static float resolveAttackSpeedDelay(PlayerCombatExtensionModule combat, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            double attackSpeed = combat.getPlayerData().getEntity().getAttributeValue(Attributes.ATTACK_SPEED);
            return (float) (20.0D / Math.max(attackSpeed, 0.001D));
        }
        double currentAttackSpeed = combat.getPlayerData().getEntity().getAttributeValue(Attributes.ATTACK_SPEED);
        double mainHandAttackSpeed = getItemAddValueModifier(combat.getPlayerData().getEntity().getMainHandItem(), Attributes.ATTACK_SPEED);
        double selectedHandAttackSpeed = getItemAddValueModifier(combat.getPlayerData().getEntity().getItemInHand(hand), Attributes.ATTACK_SPEED);
        double effectiveAttackSpeed = currentAttackSpeed - mainHandAttackSpeed + selectedHandAttackSpeed;
        return (float) (20.0D / Math.max(effectiveAttackSpeed, 0.001D));
    }

    private static double getItemAddValueModifier(ItemStack stack, Holder<Attribute> attribute) {
        final double[] total = {0.0D};
        stack.getAttributeModifiers().forEach(net.minecraft.world.entity.EquipmentSlot.MAINHAND, (holder, modifier) -> {
            if (holder.equals(attribute) && modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                total[0] += modifier.amount();
            }
        });
        return total[0];
    }
}
