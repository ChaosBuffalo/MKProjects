package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;

public class MKSizeOption extends FloatOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "mk_size");

    public MKSizeOption() {
        super(NAME);
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, Float value) {
        if (entity instanceof MKEntity) {
            ((MKEntity) entity).setRenderScale(value);
        }
    }

    @Override
    public boolean canBeBossStage() {
        return true;
    }
}
