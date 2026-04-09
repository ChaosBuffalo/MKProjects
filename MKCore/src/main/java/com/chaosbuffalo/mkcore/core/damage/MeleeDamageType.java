package com.chaosbuffalo.mkcore.core.damage;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class MeleeDamageType extends MKDamageType {

    public MeleeDamageType() {
        super(Attributes.ATTACK_DAMAGE, Attributes.ARMOR_TOUGHNESS,
                MKAttributes.MELEE_CRIT, MKAttributes.MELEE_CRIT_MULTIPLIER,
                ChatFormatting.WHITE);
    }

    @Override
    public Component getAbilityCritMessage(LivingEntity source, LivingEntity target, float damage,
                                           MKAbility ability, boolean isSelf) {
        return getAbilityCritMessage(source, target, damage, ability, isSelf, InteractionHand.MAIN_HAND);
    }

    @Override
    public Component getAbilityCritMessage(LivingEntity source, LivingEntity target, float damage,
                                           MKAbility ability, boolean isSelf, InteractionHand hand) {
        var attackStack = source.getItemInHand(hand);
        MutableComponent msg;
        if (isSelf) {
            msg = Component.translatable("mkcore.crit.melee.self",
                    target.getDisplayName(),
                    attackStack.getHoverName(),
                    Math.round(damage));
        } else {
            msg = Component.translatable("mkcore.crit.melee.other",
                    source.getDisplayName(),
                    target.getDisplayName(),
                    attackStack.getHoverName(),
                    Math.round(damage));
        }
        return msg.withStyle(ChatFormatting.GOLD);
    }

    @Override
    public float applyResistance(LivingEntity target, float originalDamage, DamageSource source) {
        return CombatRules.getDamageAfterAbsorb(target, originalDamage, source, target.getArmorValue(),
                (float) target.getAttributeValue(getResistanceAttribute()));
    }
}
