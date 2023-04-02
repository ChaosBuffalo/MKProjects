package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.capabilities.WorldNpcDataHandler;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.option_entries.INpcOptionEntry;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;

import java.util.Random;
import java.util.UUID;

public abstract class WorldPermanentOption extends NpcDefinitionOption {
    public WorldPermanentOption(ResourceLocation name, ApplyOrder order) {
        super(name, order);
    }

    public WorldPermanentOption(ResourceLocation name) {
        this(name, ApplyOrder.MIDDLE);
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, double difficultyLevel) {
        MKNpc.getWorldNpcData(entity.getCommandSenderWorld()).ifPresent(worldCap -> {
            ensureGenerated(definition, WorldNpcDataHandler.getSpawnIdForEntity(entity), worldCap);
            applyFromWorld(definition, entity, worldCap);
        });
    }

    private void ensureGenerated(NpcDefinition definition, UUID spawnId, IWorldNpcData worldNpcData) {
        if (!worldNpcData.hasEntityOptionEntry(definition, this, spawnId)) {
            generateWorldEntry(definition, spawnId, worldNpcData);
        }
    }

    protected void applyFromWorld(NpcDefinition definition, Entity entity, IWorldNpcData worldData) {
        worldData.getEntityOptionEntry(definition, this, entity).applyToEntity(entity);
    }

    public INpcOptionEntry getOptionEntry(NpcDefinition definition, UUID entityId, IWorldNpcData worldNpcData) {
        ensureGenerated(definition, entityId, worldNpcData);
        return worldNpcData.getEntityOptionEntry(definition, this, entityId);
    }

    protected abstract INpcOptionEntry makeOptionEntry(NpcDefinition definition, Random random);

    protected void generateWorldEntry(NpcDefinition definition, UUID spawnId, IWorldNpcData worldNpcData) {
        worldNpcData.addEntityOptionEntry(definition, this, spawnId,
                makeOptionEntry(definition, worldNpcData.getWorld().getRandom()));
    }
}
