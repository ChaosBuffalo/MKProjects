package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.entity.boss.BossStage;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class BossStageOption extends NpcDefinitionOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "boss_stage");
    public static final Codec<BossStageOption> CODEC = BossStage.CODEC.listOf().xmap(BossStageOption::new, i -> i.stages);

    private final List<BossStage> stages = new ArrayList<>();

    public BossStageOption(List<BossStage> stages) {
        this();
        this.stages.addAll(stages);
    }

    public BossStageOption() {
        super(NAME, ApplyOrder.MIDDLE);
    }

    public void addStage(BossStage stage) {
        this.stages.add(stage);
    }

    public BossStageOption withStage(BossStage stage) {
        addStage(stage);
        return this;
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, double difficultyLevel) {
        if (entity instanceof MKEntity mkEntity) {
            for (BossStage stage : stages) {
                BossStage copy = stage.copy();
                copy.setDefinition(definition);
                mkEntity.addBossStage(copy);
            }
        } else {
            MKNpc.LOGGER.warn("Failed to apply boss stage option {} is not an MKEntity", entity);
        }
    }
}
