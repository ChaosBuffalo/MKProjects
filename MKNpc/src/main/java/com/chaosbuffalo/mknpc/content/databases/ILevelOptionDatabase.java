package com.chaosbuffalo.mknpc.content.databases;

import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.option_entries.INpcOptionEntry;
import com.chaosbuffalo.mknpc.npc.options.WorldPermanentOption;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public interface ILevelOptionDatabase {

    boolean hasEntityOptionEntry(NpcDefinition definition, WorldPermanentOption attribute, Entity entity);

    boolean hasEntityOptionEntry(NpcDefinition definition, WorldPermanentOption attribute, UUID spawnId);

    INpcOptionEntry getEntityOptionEntry(NpcDefinition definition, WorldPermanentOption attribute,
                                         Entity entity);

    INpcOptionEntry getEntityOptionEntry(NpcDefinition definition, WorldPermanentOption attribute,
                                         UUID entityId);

    void addEntityOptionEntry(NpcDefinition definition, WorldPermanentOption attribute,
                              UUID entityId, INpcOptionEntry entry);

    Level getLevel();
}
