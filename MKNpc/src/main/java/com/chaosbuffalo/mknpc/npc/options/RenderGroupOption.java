package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.IModelLookProvider;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcOptionTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class RenderGroupOption extends NpcDefinitionOption {
    public static final ResourceLocation NAME = MKNpc.id("render_group");
    public static final Codec<RenderGroupOption> CODEC = Codec.STRING.xmap(RenderGroupOption::new, RenderGroupOption::getValue);
    public static final MapCodec<RenderGroupOption> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.STRING.fieldOf("renderGroup").forGetter(i -> i.renderGroup)
    ).apply(builder, RenderGroupOption::new));

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

    @Override
    public NpcOptionType<? extends NpcDefinitionOption> getType() {
        return NpcOptionTypes.RENDER_GROUP.get();
    }
}
