package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.content.databases.ILevelOptionDatabase;
import com.chaosbuffalo.mknpc.content.ContentDB;
import com.chaosbuffalo.mknpc.capabilities.WorldNpcDataHandler;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.option_entries.INpcOptionEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

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
        ILevelOptionDatabase levelOptions = ContentDB.getLevelOptions(entity.getLevel());
        ensureGenerated(definition, WorldNpcDataHandler.getSpawnIdForEntity(entity), levelOptions);
        applyFromWorld(definition, entity, levelOptions);
    }

    private void ensureGenerated(NpcDefinition definition, UUID spawnId, ILevelOptionDatabase levelOptions) {
        if (!levelOptions.hasEntityOptionEntry(definition, this, spawnId)) {
            generateWorldEntry(definition, spawnId, levelOptions);
        }
    }

    protected void applyFromWorld(NpcDefinition definition, Entity entity, ILevelOptionDatabase levelOptions) {
        levelOptions.getEntityOptionEntry(definition, this, entity).applyToEntity(entity);
    }

    public INpcOptionEntry getOptionEntry(NpcDefinition definition, UUID entityId, ILevelOptionDatabase levelOptions) {
        ensureGenerated(definition, entityId, levelOptions);
        return levelOptions.getEntityOptionEntry(definition, this, entityId);
    }

    protected abstract INpcOptionEntry makeOptionEntry(NpcDefinition definition, RandomSource random);

    protected void generateWorldEntry(NpcDefinition definition, UUID spawnId, ILevelOptionDatabase levelOptions) {
        levelOptions.addEntityOptionEntry(definition, this, spawnId,
                makeOptionEntry(definition, levelOptions.getLevel().getRandom()));
    }
}
