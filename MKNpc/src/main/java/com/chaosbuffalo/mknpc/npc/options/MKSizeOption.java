package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class MKSizeOption extends NpcDefinitionOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "mk_size");
    public static final Codec<MKSizeOption> CODEC = Codec.FLOAT.xmap(MKSizeOption::new, MKSizeOption::getValue);

    private final float scale;

    public MKSizeOption(float value) {
        super(NAME, ApplyOrder.MIDDLE);
        this.scale = value;
    }

    public float getValue() {
        return scale;
    }

    @Override
    public boolean canBeBossStage() {
        return true;
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, double difficultyLevel) {
        if (entity instanceof MKEntity mkEntity) {
            mkEntity.setRenderScale(scale);
        }
    }
}
