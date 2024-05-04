package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.IModelLookProvider;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class RenderGroupOption extends NpcDefinitionOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "render_group");
    public static final Codec<RenderGroupOption> CODEC = Codec.STRING.xmap(RenderGroupOption::new, RenderGroupOption::getValue);

    private final String renderGroup;

    public RenderGroupOption(String option) {
        super(NAME, ApplyOrder.MIDDLE);
        this.renderGroup = option;
    }

    public String getValue() {
        return renderGroup;
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, double difficultyLevel) {
        if (entity instanceof IModelLookProvider provider) {
            provider.setCurrentModelLook(renderGroup);
        }
    }
}
