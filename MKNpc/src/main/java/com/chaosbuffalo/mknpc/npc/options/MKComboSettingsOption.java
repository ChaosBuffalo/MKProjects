package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;

public class MKComboSettingsOption extends NpcDefinitionOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "mk_combo");
    private int comboCount;
    private int ticks;

    public MKComboSettingsOption() {
        super(NAME, ApplyOrder.MIDDLE);
        ticks = 20;
        comboCount = 1;
    }

    public MKComboSettingsOption setComboCount(int comboCount) {
        this.comboCount = comboCount;
        return this;
    }

    public MKComboSettingsOption setComboDelay(int ticks) {
        this.ticks = ticks;
        return this;
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, double difficultyLevel) {
        if (entity instanceof MKEntity) {
            ((MKEntity) entity).setAttackComboStatsAndDefault(comboCount, ticks);
        }
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("count"), ops.createInt(comboCount));
        builder.put(ops.createString("cooldown"), ops.createInt(ticks));
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        setComboCount(dynamic.get("count").asInt(1));
        setComboDelay(dynamic.get("cooldown").asInt(20));
    }
}
