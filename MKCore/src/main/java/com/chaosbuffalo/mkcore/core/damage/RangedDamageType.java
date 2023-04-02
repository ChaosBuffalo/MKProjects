package com.chaosbuffalo.mkcore.core.damage;

import com.chaosbuffalo.mkcore.core.MKAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.function.Consumer;

public class RangedDamageType extends MKDamageType {
    public RangedDamageType(ResourceLocation name) {
        super(name, MKAttributes.RANGED_DAMAGE, Attributes.ARMOR_TOUGHNESS, MKAttributes.RANGED_CRIT, MKAttributes.RANGED_CRIT_MULTIPLIER,
                ChatFormatting.DARK_BLUE);
    }

    @Override
    public void registerAttributes(Consumer<Attribute> attributeMap) {
        super.registerAttributes(attributeMap);
        attributeMap.accept(getCritChanceAttribute());
        attributeMap.accept(getCritMultiplierAttribute());
    }

    @Override
    public float getCritChance(LivingEntity source, LivingEntity target, Entity immediate) {
        float chance = super.getCritChance(source, target, immediate);
        return chance + (target.hasEffect(MobEffects.GLOWING) ? 0.05f : 0.0f);
    }

    @Override
    public float getCritMultiplier(LivingEntity source, LivingEntity livingTarget, Entity immediate) {
        float damageMultiplier = super.getCritMultiplier(source, livingTarget, immediate);
        if (livingTarget.hasEffect(MobEffects.GLOWING)) {
            damageMultiplier += 0.25f;
        }
        return damageMultiplier;
    }

    @Override
    public float applyResistance(LivingEntity target, float originalDamage) {
        return (float) (CombatRules.getDamageAfterAbsorb(originalDamage, target.getArmorValue(),
                (float) target.getAttribute(getResistanceAttribute()).getValue())
                * (1.0 - target.getAttribute(MKAttributes.RANGED_RESISTANCE).getValue()));
    }
}
