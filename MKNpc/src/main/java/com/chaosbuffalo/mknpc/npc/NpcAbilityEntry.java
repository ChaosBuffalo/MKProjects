package com.chaosbuffalo.mknpc.npc;

import com.chaosbuffalo.mknpc.MKNpc;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public class NpcAbilityEntry {
    public static final Codec<NpcAbilityEntry> CODEC = RecordCodecBuilder.<NpcAbilityEntry>mapCodec(builder -> {
        return builder.group(
                ResourceLocation.CODEC.fieldOf("abilityId").forGetter(NpcAbilityEntry::getAbilityId),
                Codec.INT.fieldOf("priority").forGetter(NpcAbilityEntry::getPriority),
                Codec.DOUBLE.fieldOf("chance").forGetter(NpcAbilityEntry::getChance)
        ).apply(builder, NpcAbilityEntry::new);
    }).codec();

    private final ResourceLocation abilityId;
    private final int priority;
    private final double chance;

    public NpcAbilityEntry(ResourceLocation abilityId, int priority, double chance) {
        this.priority = priority;
        this.abilityId = abilityId;
        this.chance = chance;
    }

    public ResourceLocation getAbilityId() {
        return abilityId;
    }

    public int getPriority() {
        return priority;
    }

    public double getChance() {
        return chance;
    }

    public <D> D serialize(DynamicOps<D> ops) {
        return CODEC.encodeStart(ops, this).getOrThrow(false, MKNpc.LOGGER::error);
    }

    public static <D> NpcAbilityEntry deserialize(DynamicOps<D> ops, D instance) {
        return CODEC.parse(new Dynamic<>(ops, instance)).getOrThrow(false, MKNpc.LOGGER::error);
    }
}
