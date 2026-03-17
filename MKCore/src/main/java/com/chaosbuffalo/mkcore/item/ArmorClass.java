package com.chaosbuffalo.mkcore.item;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.init.CoreDataMaps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;


public class ArmorClass {

    public static final Codec<ArmorClass> DIRECT_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ComponentSerialization.CODEC.fieldOf("display_name").forGetter(i -> i.name),
            Codec.unboundedMap(BuiltInRegistries.ATTRIBUTE.holderByNameCodec(), AttributeModifier.CODEC).fieldOf("positive_modifiers").forGetter(i -> i.positiveModifierMap),
            Codec.unboundedMap(BuiltInRegistries.ATTRIBUTE.holderByNameCodec(), AttributeModifier.CODEC).fieldOf("negative_modifiers").forGetter(i -> i.negativeModifierMap)
    ).apply(builder, ArmorClass::new));

    public static final Codec<Holder<ArmorClass>> CODEC = RegistryFileCodec.create(MKCoreRegistry.ARMOR_CLASS_REGISTRY_KEY, DIRECT_CODEC);
    public static final Codec<Holder<ArmorClass>> REFERENCE_CODEC = RegistryFixedCodec.create(MKCoreRegistry.ARMOR_CLASS_REGISTRY_KEY);
    public static final Codec<ResourceKey<ArmorClass>> KEY_CODEC = ResourceKey.codec(MKCoreRegistry.ARMOR_CLASS_REGISTRY_KEY);

    public static final ResourceLocation ARMOR_CLASS_POSITIVES_ID = MKCore.id("armor_class_positives");
    public static final ResourceLocation ARMOR_CLASS_NEGATIVES_ID = MKCore.id("armor_class_negatives");

    private final Component name;
    private final Map<Holder<Attribute>, AttributeModifier> positiveModifierMap = new HashMap<>();
    private final Map<Holder<Attribute>, AttributeModifier> negativeModifierMap = new HashMap<>();

    @Nullable
    public static ArmorClass get(ItemStack item) {
        var armorClassHolder = getHolder(item);
        if (armorClassHolder != null) {
            return armorClassHolder.value();
        }
        return null;
    }

    @Nullable
    public static Holder<ArmorClass> getHolder(ItemStack item) {
        return item.getItemHolder().getData(CoreDataMaps.ARMOR_CLASS_MAPPING);
    }

    private ArmorClass(Component displayName, Map<Holder<Attribute>, AttributeModifier> posMap, Map<Holder<Attribute>, AttributeModifier> negMap) {
        this.name = displayName;
        this.positiveModifierMap.putAll(posMap);
        this.negativeModifierMap.putAll(negMap);
    }

    public Component getName() {
        return name;
    }

    public Map<Holder<Attribute>, AttributeModifier> getPositiveModifierMap(EquipmentSlot slot) {
        return this.positiveModifierMap;
    }

    public Map<Holder<Attribute>, AttributeModifier> getNegativeModifierMap(EquipmentSlot slot) {
        return this.negativeModifierMap;
    }


    public static class Builder {
        private final Component name;
        private final Map<Holder<Attribute>, AttributeModifier> positiveModifierMap = new HashMap<>();
        private final Map<Holder<Attribute>, AttributeModifier> negativeModifierMap = new HashMap<>();

        public Builder(Component name) {
            this.name = name;
        }

        public Builder addNegativeEffect(Holder<Attribute> attributeIn, double amount, AttributeModifier.Operation operation) {
            if (negativeModifierMap.containsKey(attributeIn)) {
                throw new IllegalArgumentException("Cannot add 2 modifiers for the same attribute '%s' to armor class".formatted(attributeIn));
            }
            AttributeModifier attributemodifier = new AttributeModifier(ARMOR_CLASS_NEGATIVES_ID, amount, operation);
            this.negativeModifierMap.put(attributeIn, attributemodifier);
            return this;
        }

        public Builder addPositiveEffect(Holder<Attribute> attributeIn, double amount, AttributeModifier.Operation operation) {
            if (positiveModifierMap.containsKey(attributeIn)) {
                throw new IllegalArgumentException("Cannot add 2 modifiers for the same attribute '%s' to armor class".formatted(attributeIn));
            }
            AttributeModifier attributemodifier = new AttributeModifier(ARMOR_CLASS_POSITIVES_ID, amount, operation);
            this.positiveModifierMap.put(attributeIn, attributemodifier);
            return this;
        }

        public ArmorClass build() {
            return new ArmorClass(this.name, this.positiveModifierMap, this.negativeModifierMap);
        }
    }
}
