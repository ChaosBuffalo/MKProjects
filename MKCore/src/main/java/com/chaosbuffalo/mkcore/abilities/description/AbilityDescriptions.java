package com.chaosbuffalo.mkcore.abilities.description;

import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class AbilityDescriptions {

    public static Component getRangeDescription(MKAbility ability, IMKEntityData casterData) {
        String formatted = MKAbility.NUMBER_FORMATTER.format(ability.getDistance(casterData.getEntity()));
        return Component.translatable("mkcore.ability.description.range", formatted);
    }

    public static List<Component> getEffectModifiers(MobEffect effect, IMKEntityData casterData, boolean showName) {
        if (effect.attributeModifiers.isEmpty()) {
            return Collections.emptyList();
        }
        List<Component> desc = new ArrayList<>(4);
        if (showName) {
            desc.add(Component.translatable("mkcore.ability.description.effect_with_name", effect.getDisplayName()));
        } else {
            desc.add(Component.translatable("mkcore.ability.description.effect"));
        }
        for (Map.Entry<Holder<Attribute>, MobEffect.AttributeTemplate> entry : effect.attributeModifiers.entrySet()) {
            desc.add(Component.literal("    ")
                    .append(Component.translatable(entry.getKey().value().getDescriptionId()))
                    .append(String.format(": %s%.2f ", entry.getValue().amount() > 0 ? "+" : "", entry.getValue().amount()))
                    .append(Component.translatable("mkcore.ability.description.per_level")));
        }
        return desc;
    }

    public static void getEffectModifiers(MKEffect effect, AbilityContext context, boolean showName, Consumer<Component> consumer) {
        if (effect.getAttributeModifierMap().isEmpty()) {
            return;
        }
        if (showName) {
            consumer.accept(Component.translatable("mkcore.ability.description.effect_with_name", effect.getDisplayName()));
        } else {
            consumer.accept(Component.translatable("mkcore.ability.description.effect"));
        }
        for (Map.Entry<Holder<Attribute>, MKEffect.Modifier> entry : effect.getAttributeModifierMap().entrySet()) {
            MKEffect.Modifier modifier = entry.getValue();
            double value = effect.calculateModifierValue(modifier, 1,
                    modifier.skill != null ? context.getSkill(modifier.skill) : 0.0f);
            consumer.accept(Component.literal("    ")
                    .append(Component.translatable(entry.getKey().value().getDescriptionId()))
                    .append(String.format(": %s%s ", value > 0 ? "+" : "",
                            modifier.attributeModifier.operation() == AttributeModifier.Operation.ADD_VALUE ?
                                    MKAbility.NUMBER_FORMATTER.format(value) :
                                    MKAbility.PERCENT_FORMATTER.format(value)))
                    .withStyle(value > 0 ? ChatFormatting.GREEN : ChatFormatting.DARK_RED));
        }
    }
}
