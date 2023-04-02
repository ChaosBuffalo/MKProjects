package com.chaosbuffalo.mkcore.core.damage;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.function.Consumer;

public class MeleeDamageType extends MKDamageType {

    public MeleeDamageType(ResourceLocation name) {
        super(name, Attributes.ATTACK_DAMAGE, Attributes.ARMOR_TOUGHNESS,
                MKAttributes.MELEE_CRIT, MKAttributes.MELEE_CRIT_MULTIPLIER,
                ChatFormatting.WHITE);
    }

    @Override
    public Component getAbilityCritMessage(LivingEntity source, LivingEntity target, float damage,
                                           MKAbility ability, boolean isSelf) {
        TranslatableComponent msg;
        if (isSelf) {
            msg = new TranslatableComponent("mkcore.crit.melee.self",
                    target.getDisplayName(),
                    source.getMainHandItem().getHoverName(),
                    Math.round(damage));
        } else {
            msg = new TranslatableComponent("mkcore.crit.melee.other",
                    source.getDisplayName(),
                    target.getDisplayName(),
                    source.getMainHandItem().getHoverName(),
                    Math.round(damage));
        }
        return msg.withStyle(ChatFormatting.GOLD);
    }

    @Override
    public void registerAttributes(Consumer<Attribute> attributeMap) {

    }


    @Override
    public float applyResistance(LivingEntity target, float originalDamage) {
        return CombatRules.getDamageAfterAbsorb(originalDamage, target.getArmorValue(),
                (float) target.getAttribute(getResistanceAttribute()).getValue());
    }
}
