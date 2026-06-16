package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record MKWorkspacePlannerSettingsEntry(
        ResourceLocation plannerId,
        String scopeId,
        Optional<MKWorkspacePaletteOverride> paletteOverride,
        CompoundTag settings
) {
    public static final Codec<MKWorkspacePlannerSettingsEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("planner_id").forGetter(MKWorkspacePlannerSettingsEntry::plannerId),
            Codec.STRING.fieldOf("scope_id").forGetter(MKWorkspacePlannerSettingsEntry::scopeId),
            MKWorkspacePaletteOverride.CODEC.optionalFieldOf("palette_override")
                    .forGetter(MKWorkspacePlannerSettingsEntry::paletteOverride),
            CompoundTag.CODEC.optionalFieldOf("settings", new CompoundTag())
                    .forGetter(MKWorkspacePlannerSettingsEntry::settings)
    ).apply(instance, MKWorkspacePlannerSettingsEntry::new));

    public MKWorkspacePlannerSettingsEntry(ResourceLocation plannerId, String scopeId, CompoundTag settings) {
        this(plannerId, scopeId, Optional.empty(), settings);
    }

    public MKWorkspacePlannerSettingsEntry {
        if (plannerId == null) {
            throw new IllegalArgumentException("planner settings entry requires a planner id");
        }
        scopeId = scopeId == null ? "" : scopeId.trim();
        if (scopeId.isBlank()) {
            throw new IllegalArgumentException("planner settings entry requires a scope id");
        }
        paletteOverride = paletteOverride == null ? Optional.empty() :
                paletteOverride.filter(override -> !override.isEmpty());
        settings = settings == null ? new CompoundTag() : settings.copy();
    }

    public boolean isEmpty() {
        return paletteOverride.isEmpty() && settings.isEmpty();
    }

    public MKWorkspacePlannerSettingsEntry withPaletteOverride(Optional<MKWorkspacePaletteOverride> updatedOverride) {
        return new MKWorkspacePlannerSettingsEntry(plannerId, scopeId,
                updatedOverride == null ? Optional.empty() : updatedOverride, settings);
    }
}
