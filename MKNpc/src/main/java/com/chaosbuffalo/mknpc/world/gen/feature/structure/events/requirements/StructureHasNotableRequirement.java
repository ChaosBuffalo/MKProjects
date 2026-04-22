package com.chaosbuffalo.mknpc.world.gen.feature.structure.events.requirements;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.WorldStructureManager;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class StructureHasNotableRequirement extends StructureEventRequirement {
    public static final ResourceLocation TYPE_NAME = MKNpc.id("struct_requirement.has_notable");
    public static final MapCodec<StructureHasNotableRequirement> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            NpcDefinition.KEY_CODEC.fieldOf("npcDefinition").forGetter(i -> i.npcDefinition)
    ).apply(builder, StructureHasNotableRequirement::new));

    protected final ResourceKey<NpcDefinition> npcDefinition;

    public StructureHasNotableRequirement(ResourceKey<NpcDefinition> defName) {
        super(TYPE_NAME);
        this.npcDefinition = defName;
    }

    @Override
    public boolean meetsRequirements(MKStructureEntry entry,
                                     WorldStructureManager.ActiveStructure activeStructure, Level level) {
        return entry.hasNotableOfType(npcDefinition);
    }
}
