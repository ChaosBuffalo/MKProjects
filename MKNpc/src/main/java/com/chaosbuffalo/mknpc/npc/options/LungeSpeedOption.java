package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class LungeSpeedOption extends NpcDefinitionOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "lunge_speed");
    public static final Codec<LungeSpeedOption> CODEC = Codec.DOUBLE.xmap(LungeSpeedOption::new, i -> i.value);

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
}
