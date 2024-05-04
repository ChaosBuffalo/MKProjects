package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class MKComboSettingsOption extends NpcDefinitionOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "mk_combo");
    public static final Codec<MKComboSettingsOption> CODEC = RecordCodecBuilder.<MKComboSettingsOption>mapCodec(builder -> {
        return builder.group(
                Codec.INT.optionalFieldOf("cooldown", 20).forGetter(i -> i.ticks),
                Codec.INT.optionalFieldOf("count", 1).forGetter(i -> i.comboCount)
        ).apply(builder, MKComboSettingsOption::new);
    }).codec();


    private int comboCount;
    private int ticks;

    public MKComboSettingsOption(int ticks, int comboCount) {
        super(NAME, ApplyOrder.MIDDLE);
        this.ticks = ticks;
        this.comboCount = comboCount;
    }

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
        if (entity instanceof MKEntity mkEntity) {
            mkEntity.setAttackComboStatsAndDefault(comboCount, ticks);
        }
    }
}
