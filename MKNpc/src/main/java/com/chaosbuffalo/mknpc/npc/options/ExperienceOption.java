package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;

public class ExperienceOption extends IntOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "experience");

    public ExperienceOption() {
        super(NAME);
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, Integer value) {
        MKNpc.getNpcData(entity).ifPresent(cap -> cap.setBonusXp(value));
    }
}
