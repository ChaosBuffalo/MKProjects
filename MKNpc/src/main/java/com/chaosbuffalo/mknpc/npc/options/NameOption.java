package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

public class NameOption extends StringOption implements INameProvider {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "name");

    public NameOption() {
        super(NAME);
    }

    @Override
    public TextComponent getEntityName(NpcDefinition definition, Level world, UUID spawnId) {
        return new TextComponent(getValue());
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, String value) {
        if (!value.isEmpty()) {
            entity.setCustomName(new TextComponent(value));
        }
    }

    @Nullable
    @Override
    public String getDisplayName() {
        return getValue();
    }
}
