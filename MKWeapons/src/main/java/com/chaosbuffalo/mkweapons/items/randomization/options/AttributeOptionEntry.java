package com.chaosbuffalo.mkweapons.items.randomization.options;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.utils.MathUtils;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;
import java.util.UUID;

public class AttributeOptionEntry {
    private AttributeModifier modifier;
    private Attribute attribute;
    private double minValue;
    private double maxValue;

    public AttributeOptionEntry(Attribute attribute, AttributeModifier modifier, double minValue, double maxValue){
        this.modifier = modifier;
        this.attribute = attribute;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public AttributeOptionEntry(Attribute attribute, AttributeModifier modifier) {
        this(attribute, modifier, modifier.getAmount(), modifier.getAmount());
    }

    public AttributeOptionEntry(){

    }

    public AttributeModifier getModifier() {
        return modifier;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public AttributeOptionEntry copy(double difficulty){
        double finalAmount = MathUtils.lerpDouble(minValue, maxValue, difficulty / GameConstants.MAX_DIFFICULTY);
        return new AttributeOptionEntry(getAttribute(), new AttributeModifier(UUID.randomUUID(), modifier.getName(),
                finalAmount, modifier.getOperation()), minValue, maxValue);
    }

    public <D> D serialize(DynamicOps<D> ops) {
        ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
        builder.put(ops.createString("Name"), ops.createString(modifier.getName()));
        builder.put(ops.createString("Amount"), ops.createDouble(modifier.getAmount()));
        builder.put(ops.createString("Operation"), ops.createInt(modifier.getOperation().toValue()));
        builder.put(ops.createString("AttributeName"), ops.createString(ForgeRegistries.ATTRIBUTES.getKey(attribute).toString()));
        builder.put(ops.createString("minValue"), ops.createDouble(minValue));
        builder.put(ops.createString("maxValue"), ops.createDouble(maxValue));
        if (!modifier.getId().equals(Util.NIL_UUID)){
            builder.put(ops.createString("UUID"), ops.createString(modifier.getId().toString()));
        }
        return ops.createMap(builder.build());
    }


    private String getTranslationKeyForModifier(AttributeModifier.Operation op){
        switch (op){
            case MULTIPLY_BASE:
                return "mkweapons.modifier.description.percentage_base";
            case MULTIPLY_TOTAL:
                return "mkweapons.modifier.description.percentage_total";
            case ADDITION:
            default:
                return "mkweapons.modifier.description.addition";
        }
    }

    public Component getDescription(){
        String translationKey = getTranslationKeyForModifier(modifier.getOperation());
        double amount = modifier.getAmount();
        if (modifier.getOperation() != AttributeModifier.Operation.ADDITION){
            amount *= 100.0f;
        }
        return new TranslatableComponent(translationKey, I18n.get(attribute.getDescriptionId()), amount).withStyle(ChatFormatting.GRAY);
    }

    public <D> void deserialize(Dynamic<D> dynamic){
        Optional<String> name = dynamic.get("Name").asString().result();
        UUID uuid = dynamic.get("UUID").asString().result().map(UUID::fromString).orElse(Util.NIL_UUID);
        double amount = dynamic.get("Amount").asDouble(0.0);
        int op = dynamic.get("Operation").asInt(0);
        if (name.isPresent()){
            modifier = new AttributeModifier(uuid, name.get(), amount, AttributeModifier.Operation.fromValue(op));
        } else {
            MKWeapons.LOGGER.error("Failed to decode attribute modifier {} : {}", name, dynamic);
        }
        minValue = dynamic.get("minValue").asDouble(1.0);
        maxValue = dynamic.get("maxValue").asDouble(1.0);
        Optional<String> attributeName = dynamic.get("AttributeName").asString().result();
        if (attributeName.isPresent()){
            this.attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attributeName.get()));
        } else {
            MKWeapons.LOGGER.error("Failed to decode attribute name {}", dynamic);
        }
    }

}
