package com.chaosbuffalo.mknpc.world.gen.feature.structure.events.requirements;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.WorldStructureManager;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class StructureHasPoiRequirement extends StructureEventRequirement {
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKNpc.MODID,
            "struct_requirement.has_poi");
    public static final Codec<StructureHasPoiRequirement> CODEC = RecordCodecBuilder.<StructureHasPoiRequirement>mapCodec(builder -> {
        return builder.group(
                Codec.STRING.fieldOf("poiName").forGetter(i -> i.poiName)
        ).apply(builder, StructureHasPoiRequirement::new);
    }).codec();

    private final String poiName;

    public StructureHasPoiRequirement(String poiNameIn) {
        super(TYPE_NAME);
        this.poiName = poiNameIn;
    }

    @Override
    public boolean meetsRequirements(MKStructureEntry entry,
                                     WorldStructureManager.ActiveStructure activeStructure, Level world) {
        return entry.hasPoi(poiName);
    }
}
