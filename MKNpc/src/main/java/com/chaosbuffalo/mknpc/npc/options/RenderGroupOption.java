package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.IModelLookProvider;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;

public class RenderGroupOption extends StringOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "render_group");

    public RenderGroupOption() {
        super(NAME);
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, String value) {
        if (entity instanceof IModelLookProvider) {
            ((IModelLookProvider) entity).setCurrentModelLook(value);
        }
    }
}
