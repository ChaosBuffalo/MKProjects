package com.chaosbuffalo.mknpc.content;


import com.chaosbuffalo.mknpc.capabilities.IChestNpcData;
import com.chaosbuffalo.mknpc.capabilities.NpcCapabilities;
import com.chaosbuffalo.mknpc.capabilities.PointOfInterestEntry;
import com.chaosbuffalo.mknpc.npc.NotableChestEntry;
import com.chaosbuffalo.mknpc.npc.NotableNpcEntry;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.*;

public class QuestObjectDB {

    private static final List<GlobalPos> chestsToProcess = new ArrayList<>();
    private static final Map<UUID, NotableChestEntry> notableChests = new HashMap<>();
    private static final Map<UUID, NotableNpcEntry> notableNpcs = new HashMap<>();
    private static final Map<UUID, PointOfInterestEntry> pointOfInterests = new HashMap<>();

    private QuestObjectDB() {
    }

    public static void tick() {
        if (!chestsToProcess.isEmpty()) {
            chestsToProcess.forEach(QuestObjectDB::processChest);
            chestsToProcess.clear();
        }
    }

    private static void processChest(GlobalPos pos) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        Level chestLevel = server.getLevel(pos.dimension());
        if (chestLevel != null) {
            BlockEntity entity = chestLevel.getBlockEntity(pos.pos());
            if (entity != null) {
                entity.getCapability(NpcCapabilities.CHEST_NPC_DATA_CAPABILITY).ifPresent(IChestNpcData::onLoad);
            }
        }
    }

    public static void queueChestForProcessing(GlobalPos pos) {
        chestsToProcess.add(pos);
    }

    public static void putNotableChest(NotableChestEntry notableChestEntry) {
        notableChests.put(notableChestEntry.getChestId(), notableChestEntry);
    }

    public static NotableChestEntry getNotableChest(UUID id) {
        return notableChests.get(id);
    }

    public static void putNotableNpc(NotableNpcEntry notableNpcEntry) {
        notableNpcs.put(notableNpcEntry.getNotableId(), notableNpcEntry);
    }

    public static NotableNpcEntry getNotableNpc(UUID id) {
        return notableNpcs.get(id);
    }

    public static PointOfInterestEntry getPointOfInterest(UUID id) {
        return pointOfInterests.get(id);
    }

    public static void putNotablePOI(PointOfInterestEntry entry) {
        pointOfInterests.put(entry.getPointId(), entry);
    }
}
