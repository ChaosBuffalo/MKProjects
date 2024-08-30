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

public class MKSizeOption extends NpcDefinitionOption {
    public static final ResourceLocation NAME = MKNpc.id("mk_size");
    public static final Codec<MKSizeOption> CODEC = Codec.FLOAT.xmap(MKSizeOption::new, MKSizeOption::getValue);
    public static final MapCodec<MKSizeOption> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.FLOAT.fieldOf("scale").forGetter(i -> i.scale)
    ).apply(builder, MKSizeOption::new));

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
    public NpcOptionType<? extends NpcDefinitionOption> getType() {
        return NpcOptionTypes.MK_SIZE.get();
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, double difficultyLevel) {
        if (entity instanceof MKEntity mkEntity) {
            mkEntity.setRenderScale(scale);
        }
    }
}
