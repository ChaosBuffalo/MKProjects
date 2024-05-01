package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

public class NameOption extends NpcDefinitionOption implements INameProvider {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "name");
    public static final Codec<NameOption> CODEC = Codec.STRING.xmap(NameOption::new, NameOption::getValue);

    private final String name;

    public NameOption(String name) {
        super(NAME, ApplyOrder.MIDDLE);
        this.name = name;
    }

    public String getValue() {
        return name;
    }

    @Override
    public MutableComponent getEntityName(NpcDefinition definition, Level world, UUID spawnId) {
        return Component.literal(getValue());
    }

    @Nullable
    @Override
    public String getDisplayName() {
        return getValue();
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, double difficultyLevel) {
        if (!name.isEmpty()) {
            entity.setCustomName(Component.literal(name));
        }
    }
}
