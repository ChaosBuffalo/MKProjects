package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;

public class NotableOption extends BooleanOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "notable");

    public NotableOption() {
        super(NAME);
        setValue(true);
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, Boolean value) {
        MKNpc.getNpcData(entity).ifPresent(cap -> cap.setNotable(value));
        entity.setCustomNameVisible(true);
    }
}
