package com.chaosbuffalo.mknpc.world.gen.feature.structure.events.conditions;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IEntityNpcData;
import com.chaosbuffalo.mknpc.capabilities.WorldStructureManager;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class NpcDeathCountCondition extends StructureEventCondition{
    public final static ResourceLocation TYPE_NAME = MKNpc.id("struct_condition.npc_death_count");
    public static final MapCodec<NpcDeathCountCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("npcDefinition").forGetter(i -> i.npcDefinition),
            Codec.INT.fieldOf("count").forGetter(i -> i.count),
            Codec.STRING.fieldOf("name").forGetter(i -> i.name)
    ).apply(builder, NpcDeathCountCondition::new));

    private final ResourceLocation npcDefinition;
    private final int count;
    private final String name;

    public NpcDeathCountCondition(ResourceLocation npcDefinition, int count, String name) {
        super(TYPE_NAME);
        this.npcDefinition = npcDefinition;
        this.name = name;
        this.count = count;
    }

    @Override
    public void onNpcDeath(MKStructureEntry entry, WorldStructureManager.ActiveStructure activeStructure, IEntityNpcData entityData) {
        super.onNpcDeath(entry, activeStructure, entityData);
        if (entityData.getDefinition() != null && entityData.getDefinition().getDefinitionName().equals(npcDefinition)) {
            entry.getCustomData().incrementInt(name, 1);
        }
    }

    @Override
    public boolean meetsCondition(MKStructureEntry entry, WorldStructureManager.ActiveStructure activeStructure, Level world) {
        return entry.getCustomData().getInt(name) >= count;
    }

    @Override
    public void reset(MKStructureEntry entry, WorldStructureManager.ActiveStructure activeStructure) {
        super.reset(entry, activeStructure);
        entry.getCustomData().removeInt(name);
    }
}
