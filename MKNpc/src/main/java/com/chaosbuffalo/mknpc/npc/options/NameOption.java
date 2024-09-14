package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcOptionTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

public class NameOption extends NpcDefinitionOption implements INameProvider {
    public static final ResourceLocation NAME = MKNpc.id("name");
    public static final Codec<NameOption> CODEC = Codec.STRING.xmap(NameOption::new, NameOption::getValue);
    public static final MapCodec<NameOption> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.STRING.fieldOf("name").forGetter(i -> i.name)
    ).apply(builder, NameOption::new));

    private final String name;

    public NameOption(String name) {
        super(NAME, ApplyOrder.MIDDLE);
        this.name = name;
    }

    public String getValue() {
        return name;
    }

    @Override
    public MutableComponent getEntityName(NpcDefinition definition, Level level, UUID spawnId) {
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

    @Override
    public NpcOptionType<? extends NpcDefinitionOption> getType() {
        return NpcOptionTypes.NAME.get();
    }
}
