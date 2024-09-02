package com.chaosbuffalo.mkweapons.items.randomization.options;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.utils.MathUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;


public class AttributeOptionEntry {
    public static final MapCodec<AttributeOptionEntry> MAP_CODEC = RecordCodecBuilder.<AttributeOptionEntry>mapCodec(builder -> {
        return builder.group(
                BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(AttributeOptionEntry::getAttribute),
                AttributeModifier.CODEC.fieldOf("modifier").forGetter(AttributeOptionEntry::getModifier),
                EquipmentSlotGroup.CODEC
                        .optionalFieldOf("slot", EquipmentSlotGroup.ANY)
                        .forGetter(AttributeOptionEntry::getSlotGroup),
                Codec.DOUBLE.fieldOf("minValue").forGetter(i -> i.minValue),
                Codec.DOUBLE.fieldOf("maxValue").forGetter(i -> i.maxValue)
        ).apply(builder, AttributeOptionEntry::new);
    });
    public static final Codec<AttributeOptionEntry> CODEC = MAP_CODEC.codec();

    public static final StreamCodec<RegistryFriendlyByteBuf, AttributeOptionEntry> STREAM_CODEC = StreamCodec.composite(
            Attribute.STREAM_CODEC, AttributeOptionEntry::getAttribute,
            AttributeModifier.STREAM_CODEC, AttributeOptionEntry::getModifier,
            EquipmentSlotGroup.STREAM_CODEC, AttributeOptionEntry::getSlotGroup,
            ByteBufCodecs.DOUBLE, i -> i.minValue,
            ByteBufCodecs.DOUBLE, i -> i.maxValue,
            AttributeOptionEntry::new
    );


    private final AttributeModifier modifier;
    private final Holder<Attribute> attribute;
    private final double minValue;
    private final double maxValue;
    private final EquipmentSlotGroup slotGroup;

    public AttributeOptionEntry(Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup slotGroup, double minValue, double maxValue) {
        this.modifier = modifier;
        this.attribute = attribute;
        this.slotGroup = slotGroup;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public AttributeOptionEntry(Holder<Attribute> attribute, AttributeModifier modifier, double minValue, double maxValue) {
        this(attribute, modifier, EquipmentSlotGroup.ANY, minValue, maxValue);
    }

    public AttributeOptionEntry(Holder<Attribute> attribute, AttributeModifier modifier) {
        this(attribute, modifier, EquipmentSlotGroup.ANY, modifier.amount(), modifier.amount());
    }

    public AttributeOptionEntry(Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup slotGroup) {
        this(attribute, modifier, slotGroup, modifier.amount(), modifier.amount());
    }

    public AttributeModifier getModifier() {
        return modifier;
    }

    public Holder<Attribute> getAttribute() {
        return attribute;
    }

    public EquipmentSlotGroup getSlotGroup() {
        return slotGroup;
    }

    public AttributeOptionEntry createScaledModifier(double difficultyScale) {
        double finalAmount = MathUtils.lerpDouble(minValue, maxValue, difficultyScale / GameConstants.MAX_DIFFICULTY);
        return new AttributeOptionEntry(getAttribute(), new AttributeModifier(modifier.id(),
                finalAmount, modifier.operation()), slotGroup, minValue, maxValue);
    }

    private String getTranslationKeyForModifier(AttributeModifier.Operation op) {
        switch (op) {
            case ADD_MULTIPLIED_BASE:
                return "mkweapons.modifier.description.percentage_base";
            case ADD_MULTIPLIED_TOTAL:
                return "mkweapons.modifier.description.percentage_total";
            case ADD_VALUE:
            default:
                return "mkweapons.modifier.description.addition";
        }
    }

    public Component getDescription() {
        String translationKey = getTranslationKeyForModifier(modifier.operation());
        double amount = modifier.amount();
        if (modifier.operation() != AttributeModifier.Operation.ADD_VALUE) {
            amount *= 100.0f;
        }
        return Component.translatable(translationKey, I18n.get(attribute.value().getDescriptionId()), amount).withStyle(ChatFormatting.GRAY);
    }

    public <D> D serialize(DynamicOps<D> ops) {
        return CODEC.encodeStart(ops, this).getOrThrow();
    }

    public static <D> AttributeOptionEntry deserialize(Dynamic<D> dynamic) {
        return CODEC.parse(dynamic).getOrThrow();
    }
}
