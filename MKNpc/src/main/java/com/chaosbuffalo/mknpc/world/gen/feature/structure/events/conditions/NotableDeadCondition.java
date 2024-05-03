package com.chaosbuffalo.mknpc.world.gen.feature.structure.events.conditions;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.WorldStructureManager;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.npc.NotableNpcEntry;
import com.chaosbuffalo.mknpc.tile_entities.MKSpawnerTileEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class NotableDeadCondition extends StructureEventCondition {
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKNpc.MODID,
            "struct_condition.notable_dead");
    public static final Codec<NotableDeadCondition> CODEC = RecordCodecBuilder.<NotableDeadCondition>mapCodec(builder -> {
        return builder.group(
                ResourceLocation.CODEC.fieldOf("npcDefinition").forGetter(i -> i.npcDefinition),
                Codec.BOOL.fieldOf("allNotables").forGetter(i -> i.allNotables)
        ).apply(builder, NotableDeadCondition::new);
    }).codec();

    private final ResourceLocation npcDefinition;
    private final boolean allNotables;

    public NotableDeadCondition(ResourceLocation npcDefinitionName, boolean allNotablesIn) {
        super(TYPE_NAME);
        this.allNotables = allNotablesIn;
        this.npcDefinition = npcDefinitionName;

    }

    @Override
    public boolean meetsCondition(MKStructureEntry entry, WorldStructureManager.ActiveStructure activeStructure, Level world) {
        return allNotables ?
                entry.getAllNotablesOfType(npcDefinition).stream()
                        .allMatch(x -> checkSpawnerDead(x, world)) :
                entry.getFirstNotableOfType(npcDefinition)
                        .map(x -> checkSpawnerDead(x, world)).orElse(false);
    }

    private boolean checkSpawnerDead(NotableNpcEntry entry, Level world) {
        if (world.dimension() == entry.getLocation().dimension()) {
            BlockEntity entity = world.getBlockEntity(entry.getLocation().pos());
            if (entity instanceof MKSpawnerTileEntity spawner) {
                return spawner.isOnRespawnTimer();
            }
        }
        return false;

    }
}
