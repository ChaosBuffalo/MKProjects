package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcOptionTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class LungeSpeedOption extends NpcDefinitionOption {
    public static final ResourceLocation NAME = MKNpc.id("lunge_speed");
    public static final Codec<LungeSpeedOption> CODEC = Codec.DOUBLE.xmap(LungeSpeedOption::new, i -> i.value);
    public static final MapCodec<LungeSpeedOption> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.DOUBLE.fieldOf("lungeSpeed").forGetter(i -> i.value)
    ).apply(builder, LungeSpeedOption::new));

    private final double value;

    public LungeSpeedOption(double value) {
        super(NAME, ApplyOrder.MIDDLE);
        this.value = value;
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, double difficultyLevel) {
        if (entity instanceof MKEntity mkEntity) {
            mkEntity.setLungeSpeed(value);
        }
    }

    @Override
    public NpcOptionType<? extends NpcDefinitionOption> getType() {
        return NpcOptionTypes.LUNGE_SPEED.get();
    }
}
