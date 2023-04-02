package com.chaosbuffalo.mkcore.abilities.description;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AbilityDescriptions {

    public static Component getRangeDescription(MKAbility ability, IMKEntityData casterData) {
        return new TranslatableComponent("mkcore.ability.description.range", ability.getDistance(casterData.getEntity()));
    }

    public static List<Component> getEffectModifiers(MobEffect effect, IMKEntityData casterData, boolean showName) {
        if (effect.getAttributeModifiers().isEmpty()) {
            return Collections.emptyList();
        }
        List<Component> desc = new ArrayList<>(4);
        if (showName) {
            desc.add(new TranslatableComponent("mkcore.ability.description.effect_with_name", effect.getDisplayName()));
        } else {
            desc.add(new TranslatableComponent("mkcore.ability.description.effect"));
        }
        for (Map.Entry<Attribute, AttributeModifier> entry : effect.getAttributeModifiers().entrySet()) {
            desc.add(new TextComponent("    ")
                    .append(new TranslatableComponent(entry.getKey().getDescriptionId()))
                    .append(String.format(": %s%.2f ", entry.getValue().getAmount() > 0 ? "+" : "", entry.getValue().getAmount()))
                    .append(new TranslatableComponent("mkcore.ability.description.per_level")));
        }
        return desc;
    }

    public static List<Component> getEffectModifiers(MKEffect effect, IMKEntityData casterData, boolean showName) {
        if (effect.getAttributeModifierMap().isEmpty()) {
            return Collections.emptyList();
        }
        List<Component> desc = new ArrayList<>(4);
        if (showName) {
            desc.add(new TranslatableComponent("mkcore.ability.description.effect_with_name", effect.getDisplayName()));
        } else {
            desc.add(new TranslatableComponent("mkcore.ability.description.effect"));
        }
        for (Map.Entry<Attribute, MKEffect.Modifier> entry : effect.getAttributeModifierMap().entrySet()) {
            MKEffect.Modifier modifier = entry.getValue();
            double value = effect.calculateModifierValue(modifier, 1,
                    modifier.skill != null ? MKAbility.getSkillLevel(casterData.getEntity(), modifier.skill) : 0.0f);
            desc.add(new TextComponent("    ")
                    .append(new TranslatableComponent(entry.getKey().getDescriptionId()))
                    .append(String.format(": %s%s ", value > 0 ? "+" : "",
                            modifier.attributeModifier.getOperation() == AttributeModifier.Operation.ADDITION ?
                                    MKAbility.NUMBER_FORMATTER.format(value) :
                                    MKAbility.PERCENT_FORMATTER.format(value)))
                    .withStyle(value > 0 ? ChatFormatting.GREEN : ChatFormatting.DARK_RED));
        }
        return desc;
    }
}
