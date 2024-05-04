package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class NotableOption extends NpcDefinitionOption {
    public static final Codec<NotableOption> CODEC = Codec.BOOL.xmap(NotableOption::new, i -> i.value);
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "notable");

    private final boolean value;

    public NotableOption(boolean notable) {
        super(NAME, ApplyOrder.MIDDLE);
        this.value = notable;
    }

    public NotableOption() {
        this(true);
    }

    public boolean isNotable() {
        return value;
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, double difficultyLevel) {
        MKNpc.getNpcData(entity).ifPresent(cap -> cap.setNotable(value));
        entity.setCustomNameVisible(true);
    }
}
