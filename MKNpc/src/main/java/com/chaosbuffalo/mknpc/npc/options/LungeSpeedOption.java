package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;

public class LungeSpeedOption extends DoubleOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "lunge_speed");

    public LungeSpeedOption() {
        super(NAME);
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, Double value) {
        if (entity instanceof MKEntity) {
            ((MKEntity) entity).setLungeSpeed(value);
        }
    }
}
