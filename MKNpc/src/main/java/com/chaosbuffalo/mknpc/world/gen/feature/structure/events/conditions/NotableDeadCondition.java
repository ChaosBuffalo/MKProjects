package com.chaosbuffalo.mknpc.world.gen.feature.structure.events.conditions;

import com.chaosbuffalo.mkcore.serialization.attributes.BooleanAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.WorldStructureManager;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.npc.NotableNpcEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinitionManager;
import com.chaosbuffalo.mknpc.tile_entities.MKSpawnerTileEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class NotableDeadCondition extends StructureEventCondition{
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKNpc.MODID,
            "struct_condition.notable_dead");
    protected ResourceLocationAttribute npcDefinition = new ResourceLocationAttribute(
            "npcDefinition", NpcDefinitionManager.INVALID_NPC_DEF);
    protected BooleanAttribute allNotables = new BooleanAttribute("allNotables", false);

    public NotableDeadCondition(ResourceLocation npcDefinitionName, boolean allNotablesIn) {
        this();
        this.allNotables.setValue(allNotablesIn);
        npcDefinition.setValue(npcDefinitionName);

    }

    public NotableDeadCondition() {
        super(TYPE_NAME);
        addAttributes(npcDefinition, allNotables);
    }

    @Override
    public boolean meetsCondition(MKStructureEntry entry, WorldStructureManager.ActiveStructure activeStructure, Level world) {
        return allNotables.value() ? entry.getAllNotablesOfType(npcDefinition.getValue()).stream()
                .allMatch(x -> checkSpawnerDead(x, world)) : entry.getFirstNotableOfType(npcDefinition.getValue())
                .map(x -> checkSpawnerDead(x, world)).orElse(false);
    }

    private boolean checkSpawnerDead(NotableNpcEntry entry, Level world) {
        if (world.dimension() == entry.getLocation().dimension()) {
            BlockEntity entity = world.getBlockEntity(entry.getLocation().pos());
            if (entity instanceof MKSpawnerTileEntity) {
                return ((MKSpawnerTileEntity) entity).isOnRespawnTimer();
            }
        }
        return false;

    }
}
