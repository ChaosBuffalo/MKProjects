package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;

public class GhostOption extends FloatOption{
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "ghost");

    public GhostOption() {
        super(NAME);
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, Float value) {
        if (entity instanceof MKEntity){
            ((MKEntity) entity).setGhost(true);
            ((MKEntity) entity).setGhostTranslucency(value);
        }
    }

}
