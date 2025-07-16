package com.chaosbuffalo.mknpc.npc.option_entries;

import com.chaosbuffalo.mknpc.npc.NpcOptionEntryTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;


public class FactionNameOptionEntry implements INpcOptionEntry, INameEntry {
    public static final MapCodec<FactionNameOptionEntry> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ComponentSerialization.CODEC.fieldOf("name").forGetter(i -> i.displayName)
    ).apply(builder, FactionNameOptionEntry::new));

    private final Component displayName;

    public FactionNameOptionEntry(String name) {
        this.displayName = Component.literal(name);
    }

    public FactionNameOptionEntry(Component name) {
        this.displayName = name;
    }

    @Override
    public void applyToEntity(Entity entity) {
        if (entity instanceof LivingEntity) {
            entity.setCustomName(displayName);
        }
    }

    @Override
    public NpcOptionEntryType<? extends INpcOptionEntry> getType() {
        return NpcOptionEntryTypes.FACTION_NAME.get();
    }

    @Override
    public MutableComponent getName() {
        return displayName.copy();
    }
}
