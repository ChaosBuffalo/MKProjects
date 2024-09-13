package com.chaosbuffalo.mknpc.world.gen.feature.structure.events.requirements;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.WorldStructureManager;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class StructureHasNpcRequirement extends StructureEventRequirement{
    public final static ResourceLocation TYPE_NAME = MKNpc.id("struct_requirement.has_npc");
    public static final MapCodec<StructureHasNpcRequirement> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("npcDefinition").forGetter(i -> i.npcDefinition)
    ).apply(builder, StructureHasNpcRequirement::new));

    protected final ResourceLocation npcDefinition;

    public StructureHasNpcRequirement(ResourceLocation defName) {
        super(TYPE_NAME);
        this.npcDefinition = defName;
    }


    @Override
    public boolean meetsRequirements(MKStructureEntry entry, WorldStructureManager.ActiveStructure activeStructure, Level world) {
        return entry.hasNpc(npcDefinition, world.registryAccess());
    }
}
