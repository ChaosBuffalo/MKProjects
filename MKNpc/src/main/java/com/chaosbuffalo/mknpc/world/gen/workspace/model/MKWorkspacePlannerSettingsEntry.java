package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public record MKWorkspacePlannerSettingsEntry(
        ResourceLocation plannerId,
        CompoundTag settings
) {
    public static final Codec<MKWorkspacePlannerSettingsEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("planner_id").forGetter(MKWorkspacePlannerSettingsEntry::plannerId),
            CompoundTag.CODEC.optionalFieldOf("settings", new CompoundTag())
                    .forGetter(MKWorkspacePlannerSettingsEntry::settings)
    ).apply(instance, MKWorkspacePlannerSettingsEntry::new));

    public MKWorkspacePlannerSettingsEntry {
        if (plannerId == null) {
            throw new IllegalArgumentException("planner settings entry requires a planner id");
        }
        settings = settings == null ? new CompoundTag() : settings.copy();
    }
}
