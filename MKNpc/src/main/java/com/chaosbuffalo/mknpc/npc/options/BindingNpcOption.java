package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.capabilities.WorldNpcDataHandler;
import com.chaosbuffalo.mknpc.content.ContentDB;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.options.binding.IBoundNpcOptionValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class BindingNpcOption extends NpcDefinitionOption {
    public BindingNpcOption(ResourceLocation name, ApplyOrder order) {
        super(name, order);
    }

    public BindingNpcOption(ResourceLocation name) {
        this(name, ApplyOrder.MIDDLE);
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, double difficultyLevel) {
        ContentDB.tryGetLevelData(entity.getLevel()).ifPresent(worldCap -> {
            UUID spawnId = WorldNpcDataHandler.getSpawnIdForEntity(entity);
            IBoundNpcOptionValue value = getOrCreateBoundValue(definition, spawnId, worldCap);
            if (value != null) {
                value.applyToEntity(entity);
            }
        });
    }

    @Nullable
    protected IBoundNpcOptionValue getOrCreateBoundValue(NpcDefinition definition, UUID spawnId, IWorldNpcData worldNpcData) {
        if (!worldNpcData.hasBoundOptionValue(definition, this, spawnId)) {
            IBoundNpcOptionValue entry = generateBoundValue(definition, worldNpcData.getWorld().getRandom());
            if (entry == null) {
                return null;
            }
            worldNpcData.addBoundOptionValue(definition, this, spawnId, entry);
            return entry;
        }

        return worldNpcData.getBoundOptionValue(definition, this, spawnId);
    }

    @Nullable
    protected abstract IBoundNpcOptionValue generateBoundValue(NpcDefinition definition, RandomSource random);

}
