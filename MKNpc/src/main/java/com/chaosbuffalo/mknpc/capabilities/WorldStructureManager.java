package com.chaosbuffalo.mknpc.capabilities;


import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.event.WorldStructureHandler;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawStructure;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;

import java.util.*;
import java.util.function.BiConsumer;

public class WorldStructureManager {

    public class ActivePlayerEntry {
        public int ticksSinceSeen;
        public final ServerPlayer player;

        public ActivePlayerEntry(ServerPlayer player) {
            ticksSinceSeen = 0;
            this.player = player;
        }
    }

    public class ActiveStructure {
        private int ticksEmpty = 0;
        private final UUID structureId;
        private final Map<UUID, ActivePlayerEntry> activePlayers;
        private final int PLAYER_TIMEOUT = 20 * 5;
        private final int EMPTY_TIMEOUT = 20 * 60;
        private final BiConsumer<ServerPlayer, ActiveStructure> playerRemoveCallback;

        public ActiveStructure(UUID structureId, BiConsumer<ServerPlayer, ActiveStructure> removalCallback){
            this.activePlayers = new HashMap<>();
            this.structureId = structureId;
            this.playerRemoveCallback = removalCallback;
        }

        public Map<UUID, ActivePlayerEntry> getActivePlayers() {
            return activePlayers;
        }

        public UUID getStructureId() {
            return structureId;
        }

        private void addPlayer(ServerPlayer player) {
            activePlayers.put(player.getUUID(), new ActivePlayerEntry(player));
        }

        private void removePlayer(UUID uuid) {
            ActivePlayerEntry entry = activePlayers.get(uuid);
            if (entry != null) {
                playerRemoveCallback.accept(entry.player, this);
            }
            activePlayers.remove(uuid);
        }

        // returns true if we are not already in structure
        public boolean visit(ServerPlayer player) {
            if (activePlayers.containsKey(player.getUUID())) {
                ActivePlayerEntry active = activePlayers.get(player.getUUID());
                active.ticksSinceSeen = 0;
                return false;
            } else {
                addPlayer(player);
                return true;
            }
        }

        public boolean tick() {
            Set<UUID> toRemove = new HashSet<>();
            for (Map.Entry<UUID, ActivePlayerEntry> entry : activePlayers.entrySet()) {
                entry.getValue().ticksSinceSeen++;
                if (entry.getValue().ticksSinceSeen > PLAYER_TIMEOUT || !entry.getValue().player.isAlive()) {
                    toRemove.add(entry.getKey());
                }
            }
            for (UUID rem : toRemove) {
                removePlayer(rem);
            }
            if (activePlayers.isEmpty()) {
                ticksEmpty++;
            } else {
                ticksEmpty = 0;
            }
            return ticksEmpty > PLAYER_TIMEOUT;
        }


    }

    Map<UUID, ActiveStructure> activeStructures;
    private final WorldNpcDataHandler handler;

    public WorldStructureManager(WorldNpcDataHandler handler) {
        activeStructures = new HashMap<>();
        this.handler = handler;
    }

    public void visitStructure(UUID structureId, ServerPlayer player) {
        ActiveStructure struct = activeStructures.computeIfAbsent(structureId, (id) -> {
            ActiveStructure activeStructure = new ActiveStructure(id, this::removePlayer);
            MKStructureEntry structureEntry = handler.getStructureData(id);
            if (structureEntry != null) {
                ConfiguredStructureFeature<?, MKJigsawStructure> configuredStruct = WorldStructureHandler.MK_STRUCTURE_INDEX.get(structureEntry.getStructureName());
                if (configuredStruct != null) {
                    configuredStruct.feature.onStructureActivate(structureEntry, activeStructure, handler.getWorld());
                }
            }
            return activeStructure;
        });
        if (struct.visit(player)) {
            MKStructureEntry entry = handler.getStructureData(structureId);
            if (entry != null) {
                MKNpc.LOGGER.debug("Player {} entering structure {} (ID: {})", player, entry.getStructureName(), structureId);
                ConfiguredStructureFeature<?, MKJigsawStructure> configuredStruct = WorldStructureHandler.MK_STRUCTURE_INDEX.get(entry.getStructureName());
                if (configuredStruct != null) {
                    configuredStruct.feature.onPlayerEnter(player, entry, struct);
                }
            }
        }
    }

    public void removePlayer(ServerPlayer player, ActiveStructure activeStructure) {
        MKStructureEntry entry = handler.getStructureData(activeStructure.getStructureId());
        if (player != null && entry != null) {
            MKNpc.LOGGER.debug("Player {} exiting structure {} (ID: {})", player, entry.getStructureName(), activeStructure.getStructureId());
            ConfiguredStructureFeature<?, MKJigsawStructure> configuredStruct = WorldStructureHandler.MK_STRUCTURE_INDEX.get(entry.getStructureName());
            if (configuredStruct != null) {
                configuredStruct.feature.onPlayerExit(player, entry, activeStructure);
            }
        }

    }

    public void onNpcDeath(IEntityNpcData npcData) {
        npcData.getStructureId().ifPresent(structureId -> {
            if (activeStructures.containsKey(structureId)) {
                MKStructureEntry entry = handler.getStructureData(structureId);
                ActiveStructure activeStruct = activeStructures.get(structureId);
                if (entry != null && activeStruct != null) {
                    ConfiguredStructureFeature<?, MKJigsawStructure> configuredStruct = WorldStructureHandler.MK_STRUCTURE_INDEX.get(entry.getStructureName());
                    if (configuredStruct != null) {
                        configuredStruct.feature.onNpcDeath(entry, activeStruct, npcData);
                    }
                }
            }
        });

    }

    public void tick(){
        if (activeStructures.isEmpty()) {
            return;
        }
        Set<UUID> toRemove = new HashSet<>();
        for (Map.Entry<UUID, ActiveStructure> entry : activeStructures.entrySet()) {
            if (entry.getValue().tick()) {
                toRemove.add(entry.getKey());
            }
            MKStructureEntry structureEntry = handler.getStructureData(entry.getValue().structureId);
            if (structureEntry != null) {
                ConfiguredStructureFeature<?, MKJigsawStructure> configuredStruct = WorldStructureHandler.MK_STRUCTURE_INDEX.get(structureEntry.getStructureName());
                if (configuredStruct != null) {
                    structureEntry.getCooldownTracker().tick();
                    configuredStruct.feature.onActiveTick(structureEntry, entry.getValue(), handler.getWorld());
                }
            }
        }
        for (UUID structId : toRemove) {
            MKStructureEntry structureEntry = handler.getStructureData(structId);
            if (structureEntry != null) {
                ConfiguredStructureFeature<?, MKJigsawStructure> configuredStruct = WorldStructureHandler.MK_STRUCTURE_INDEX.get(structureEntry.getStructureName());
                if (configuredStruct != null) {
                    configuredStruct.feature.onStructureDeactivate(structureEntry, activeStructures.get(structId), handler.getWorld());
                }
            }
            activeStructures.remove(structId);
        }
    }
}
