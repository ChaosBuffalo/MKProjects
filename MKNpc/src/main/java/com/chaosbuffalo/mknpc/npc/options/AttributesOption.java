package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcAttributeEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

import java.util.ArrayList;
import java.util.List;

public class AttributesOption extends NpcDefinitionOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "attributes");
    public static final Codec<AttributesOption> CODEC = NpcAttributeEntry.CODEC.listOf().xmap(AttributesOption::new, i -> i.attributes);

    private final List<NpcAttributeEntry> attributes;

    private AttributesOption(List<NpcAttributeEntry> list) {
        super(NAME, ApplyOrder.MIDDLE);
        attributes = ImmutableList.copyOf(list);
    }

    public AttributesOption() {
        super(NAME, ApplyOrder.MIDDLE);
        attributes = new ArrayList<>();
    }

    public AttributesOption addAttributeEntry(NpcAttributeEntry entry) {
        attributes.add(entry);
        return this;
    }

    @Override
    public boolean canBeBossStage() {
        return true;
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, double difficultyLevel) {
        if (entity instanceof LivingEntity living) {
            AttributeMap manager = living.getAttributes();
            for (NpcAttributeEntry entry : attributes) {
                AttributeInstance instance = manager.getInstance(entry.getAttribute());
                if (instance != null) {
                    instance.setBaseValue(entry.getValue());
                }
            }
        }
    }
}
