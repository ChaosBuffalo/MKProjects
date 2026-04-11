package com.chaosbuffalo.mkcore.core.combat;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.events.ModifyBaseMeleeDamageEvent;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

public final class MeleeHandStatsResolver {
    private MeleeHandStatsResolver() {
    }

    public static int getRequiredAttackStrengthTicks(LivingEntity entity, InteractionHand hand) {
        return Math.max(1, Mth.ceil(resolveAttackSpeedDelay(entity, hand)));
    }

    public static float resolveAttackDamage(IMKEntityData entityData, InteractionHand hand) {
        LivingEntity entity = entityData.getEntity();
        float damage;
        if (hand == InteractionHand.MAIN_HAND) {
            damage = (float) entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
        } else {
            double currentAttackDamage = entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
            double mainHandAttackDamage = getItemAddValueModifier(entity.getMainHandItem(), Attributes.ATTACK_DAMAGE);
            double selectedHandAttackDamage = getItemAddValueModifier(entity.getItemInHand(hand), Attributes.ATTACK_DAMAGE);
            damage = (float) (currentAttackDamage - mainHandAttackDamage + selectedHandAttackDamage);
        }
        ModifyBaseMeleeDamageEvent event = new ModifyBaseMeleeDamageEvent(entityData, hand, entity.getItemInHand(hand), damage);
        NeoForge.EVENT_BUS.post(event);
        return event.getDamage();
    }

    public static float resolveAttackKnockback(LivingEntity entity, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            return (float) entity.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        }
        double currentAttackKnockback = entity.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        double mainHandAttackKnockback = getItemAddValueModifier(entity.getMainHandItem(), Attributes.ATTACK_KNOCKBACK);
        double selectedHandAttackKnockback = getItemAddValueModifier(entity.getItemInHand(hand), Attributes.ATTACK_KNOCKBACK);
        return (float) (currentAttackKnockback - mainHandAttackKnockback + selectedHandAttackKnockback);
    }

    public static float resolveAttackSpeedDelay(LivingEntity entity, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            double attackSpeed = entity.getAttributeValue(Attributes.ATTACK_SPEED);
            return (float) (20.0D / Math.max(attackSpeed, 0.001D));
        }
        double currentAttackSpeed = entity.getAttributeValue(Attributes.ATTACK_SPEED);
        double mainHandAttackSpeed = getItemAddValueModifier(entity.getMainHandItem(), Attributes.ATTACK_SPEED);
        double selectedHandAttackSpeed = getItemAddValueModifier(entity.getItemInHand(hand), Attributes.ATTACK_SPEED);
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
