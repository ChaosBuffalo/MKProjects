package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.entity.boss.BossStage;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcOptionTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class BossStageOption extends NpcDefinitionOption {
    public static final ResourceLocation NAME = MKNpc.id("boss_stage");
    public static final Codec<BossStageOption> CODEC = BossStage.CODEC.listOf().xmap(BossStageOption::new, i -> i.stages);
    public static final MapCodec<BossStageOption> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            BossStage.CODEC.listOf().fieldOf("stages").forGetter(i -> i.stages)
    ).apply(builder, BossStageOption::new));


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

    @Override
    public NpcOptionType<? extends NpcDefinitionOption> getType() {
        return NpcOptionTypes.BOSS_STAGE.get();
    }
}
