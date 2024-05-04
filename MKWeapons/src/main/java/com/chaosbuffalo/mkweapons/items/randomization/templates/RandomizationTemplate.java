package com.chaosbuffalo.mkweapons.items.randomization.templates;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.randomization.slots.IRandomizationSlot;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.List;

public class RandomizationTemplate {
    public static final Codec<RandomizationTemplate> CODEC = RecordCodecBuilder.<RandomizationTemplate>mapCodec(builder -> {
        return builder.group(
                ResourceLocation.CODEC.fieldOf("name").forGetter(RandomizationTemplate::getName),
                IRandomizationSlot.CODEC.listOf().fieldOf("slots").forGetter(RandomizationTemplate::getRandomizationSlots)
        ).apply(builder, RandomizationTemplate::new);
    }).codec();

    private final ResourceLocation name;
    private final List<IRandomizationSlot> slots;

    public RandomizationTemplate(ResourceLocation name) {
        this(name, List.of());
    }

    public RandomizationTemplate(ResourceLocation name, List<IRandomizationSlot> slots) {
        this.name = name;
        this.slots = slots;
    }

    public RandomizationTemplate(ResourceLocation name, IRandomizationSlot... slots) {
        this(name, Arrays.asList(slots));
    }

    public ResourceLocation getName() {
        return name;
    }

    public List<IRandomizationSlot> getRandomizationSlots() {
        return slots;
    }

    public <D> D serialize(DynamicOps<D> ops) {
        return CODEC.encodeStart(ops, this).getOrThrow(false, MKWeapons.LOGGER::error);
    }

    public static <D> RandomizationTemplate deserialize(Dynamic<D> dynamic) {
        return CODEC.parse(dynamic).getOrThrow(false, MKWeapons.LOGGER::error);
    }
}
