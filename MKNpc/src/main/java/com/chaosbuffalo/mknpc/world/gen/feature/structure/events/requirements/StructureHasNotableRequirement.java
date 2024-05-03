package com.chaosbuffalo.mknpc.world.gen.feature.structure.events.requirements;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.WorldStructureManager;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class StructureHasNotableRequirement extends StructureEventRequirement {
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKNpc.MODID,
            "struct_requirement.has_notable");
    public static final Codec<StructureHasNotableRequirement> CODEC = RecordCodecBuilder.<StructureHasNotableRequirement>mapCodec(builder -> {
        return builder.group(
                ResourceLocation.CODEC.fieldOf("npcDefinition").forGetter(i -> i.npcDefinition)
        ).apply(builder, StructureHasNotableRequirement::new);
    }).codec();

    protected final ResourceLocation npcDefinition;

    public StructureHasNotableRequirement(ResourceLocation defName) {
        super(TYPE_NAME);
        this.npcDefinition = defName;
    }

    @Override
    public boolean meetsRequirements(MKStructureEntry entry,
                                     WorldStructureManager.ActiveStructure activeStructure, Level world) {
        return entry.hasNotableOfType(npcDefinition);
    }
}
