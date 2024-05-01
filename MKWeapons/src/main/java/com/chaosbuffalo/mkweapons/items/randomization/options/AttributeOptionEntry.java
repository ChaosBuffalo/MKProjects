package com.chaosbuffalo.mkweapons.items.randomization.options;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.utils.CommonCodecs;
import com.chaosbuffalo.mkcore.utils.MathUtils;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;


public class AttributeOptionEntry {
    public static final Codec<AttributeModifier> MODIFIER_OPTIONAL_ID_CODEC = RecordCodecBuilder.<AttributeModifier>mapCodec(builder -> {
        return builder.group(
                UUIDUtil.STRING_CODEC.optionalFieldOf("id", Util.NIL_UUID).forGetter(AttributeModifier::getId),
                Codec.STRING.fieldOf("name").forGetter(AttributeModifier::getName),
                Codec.DOUBLE.fieldOf("amount").forGetter(AttributeModifier::getAmount),
                CommonCodecs.ATTRIBUTE_MODIFIER_OPERATION_CODEC.fieldOf("operation").forGetter(AttributeModifier::getOperation)
        ).apply(builder, AttributeModifier::new);
    }).codec();

    public static final Codec<AttributeOptionEntry> CODEC = RecordCodecBuilder.<AttributeOptionEntry>mapCodec(builder -> {
        return builder.group(
                ForgeRegistries.ATTRIBUTES.getCodec().fieldOf("attribute").forGetter(AttributeOptionEntry::getAttribute),
                MODIFIER_OPTIONAL_ID_CODEC.fieldOf("modifier").forGetter(AttributeOptionEntry::getModifier),
                Codec.DOUBLE.fieldOf("minValue").forGetter(i -> i.minValue),
                Codec.DOUBLE.fieldOf("maxValue").forGetter(i -> i.maxValue)
        ).apply(builder, AttributeOptionEntry::new);
    }).codec();

    private final AttributeModifier modifier;
    private final Attribute attribute;
    private final double minValue;
    private final double maxValue;

    public AttributeOptionEntry(Attribute attribute, AttributeModifier modifier, double minValue, double maxValue) {
        this.modifier = modifier;
        this.attribute = attribute;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public AttributeOptionEntry(Attribute attribute, AttributeModifier modifier) {
        this(attribute, modifier, modifier.getAmount(), modifier.getAmount());
    }

    public AttributeModifier getModifier() {
        return modifier;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public AttributeOptionEntry copy(double difficulty) {
        double finalAmount = MathUtils.lerpDouble(minValue, maxValue, difficulty / GameConstants.MAX_DIFFICULTY);
        return new AttributeOptionEntry(getAttribute(), new AttributeModifier(UUID.randomUUID(), modifier.getName(),
                finalAmount, modifier.getOperation()), minValue, maxValue);
    }

    public AttributeOptionEntry createScaledModifier(double difficultyScale) {
        return getModifier().getId().equals(Util.NIL_UUID) ? copy(difficultyScale) : this;
    }


    private String getTranslationKeyForModifier(AttributeModifier.Operation op) {
        switch (op) {
            case MULTIPLY_BASE:
                return "mkweapons.modifier.description.percentage_base";
            case MULTIPLY_TOTAL:
                return "mkweapons.modifier.description.percentage_total";
            case ADDITION:
            default:
                return "mkweapons.modifier.description.addition";
        }
    }

    public Component getDescription() {
        String translationKey = getTranslationKeyForModifier(modifier.getOperation());
        double amount = modifier.getAmount();
        if (modifier.getOperation() != AttributeModifier.Operation.ADDITION) {
            amount *= 100.0f;
        }
        return Component.translatable(translationKey, I18n.get(attribute.getDescriptionId()), amount).withStyle(ChatFormatting.GRAY);
    }

    public <D> D serialize(DynamicOps<D> ops) {
        return CODEC.encodeStart(ops, this).getOrThrow(false, MKWeapons.LOGGER::error);
    }

    public static <D> AttributeOptionEntry deserialize(Dynamic<D> dynamic) {
        return CODEC.parse(dynamic).getOrThrow(false, MKWeapons.LOGGER::error);
    }
}
