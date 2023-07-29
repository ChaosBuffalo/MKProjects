package com.chaosbuffalo.mknpc.capabilities;


import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

public class WorldStructureManager {

    public static class ActivePlayerEntry {
        public int ticksSinceSeen;
        public final ServerPlayer player;

        public ActivePlayerEntry(ServerPlayer player) {
            ticksSinceSeen = 0;
            this.player = player;
        }
    }

    public static class ActiveEntityEntry {
        private final Entity entity;
        private final String eventName;

        public ActiveEntityEntry(Entity entity, String eventName) {
            this.entity = entity;
            this.eventName = eventName;
        }

        public String getEventName() {
            return eventName;
        }

        public Entity getEntity() {
            return entity;
        }
    }

    public static class ActiveStructure {
        private int ticksEmpty = 0;
        private final UUID structureId;
        private final Map<UUID, ActivePlayerEntry> activePlayers;


        private final Map<UUID, ActiveEntityEntry> entities;
        private final int PLAYER_TIMEOUT = 20 * 5;
        private final int EMPTY_TIMEOUT = 20 * 60;
        private final BiConsumer<ServerPlayer, ActiveStructure> playerRemoveCallback;

        public ActiveStructure(UUID structureId, BiConsumer<ServerPlayer, ActiveStructure> removalCallback) {
            this.activePlayers = new HashMap<>();
            this.entities = new HashMap<>();
            this.structureId = structureId;
            this.playerRemoveCallback = removalCallback;
        }

        public Map<UUID, ActivePlayerEntry> getActivePlayers() {
            return activePlayers;
        }

        public UUID getStructureId() {
            return structureId;
        }

        public void addEntity(UUID id, Entity entity, String eventName) {
            entities.put(id, new ActiveEntityEntry(entity, eventName));
        }

        public void entityDied(UUID id) {
            entities.remove(id);
        }

        @Nullable
        public ActiveEntityEntry getActiveEntity(UUID id) {
            return entities.get(id);
        }

        private void addPlayer(ServerPlayer player) {
            activePlayers.put(player.getUUID(), new ActivePlayerEntry(player));
        }

        public boolean hasActiveEntity(UUID id) {
            return entities.containsKey(id);
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
            ActivePlayerEntry active = activePlayers.get(player.getUUID());
            if (active != null) {
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
            handler.getStructureData(id).ifPresent(structureEntry ->
                    structureEntry.getStructure().ifPresent(structure ->
                            structure.onStructureActivate(structureEntry, activeStructure, handler.getWorld())));
            return activeStructure;
        });
        if (struct.visit(player)) {
            handler.getStructureData(structureId).ifPresent(entry -> {
                MKNpc.LOGGER.debug("Player {} entering structure {} (ID: {})",
                        player, entry.getStructureName(), structureId);
                entry.getStructure().ifPresent(structure -> {
                    structure.onPlayerEnter(player, entry, struct);
                });
            });
        }
    }

    public void removePlayer(ServerPlayer player, ActiveStructure activeStructure) {
        Optional<MKStructureEntry> entry = handler.getStructureData(activeStructure.getStructureId());
        if (player != null) {
            entry.ifPresent(structureEntry -> {
                MKNpc.LOGGER.debug("Player {} exiting structure {} (ID: {})",
                        player, structureEntry.getStructureName(), activeStructure.getStructureId());
                structureEntry.getStructure().ifPresent(structure -> {
                    structure.onPlayerExit(player, structureEntry, activeStructure);
                });
            });

        }

    }

    public void onNpcDeath(IEntityNpcData npcData) {
        npcData.getStructureId().ifPresent(structureId -> {
            if (activeStructures.containsKey(structureId)) {
                Optional<MKStructureEntry> entry = handler.getStructureData(structureId);
                ActiveStructure activeStruct = activeStructures.get(structureId);
                if (activeStruct != null) {
                    entry.ifPresent(structureEntry ->
                            structureEntry.getStructure().ifPresent(structure ->
                                    structure.onNpcDeath(structureEntry, activeStruct, npcData)));
                    ActiveEntityEntry entEntry = activeStruct.getActiveEntity(npcData.getNotableUUID());
                    if (entEntry != null) {
                        entry.ifPresent(structureEntry ->
                                structureEntry.getStructure().ifPresent(structure ->
                                        structure.onTrackedEntityDeath(structureEntry, activeStruct, npcData,
                                                entEntry.getEventName())));
                        activeStruct.entityDied(npcData.getNotableUUID());
                    }

                }
            }
        });

    }

    public void tick() {
        if (activeStructures.isEmpty()) {
            return;
        }
        Set<UUID> toRemove = new HashSet<>();
        for (Map.Entry<UUID, ActiveStructure> entry : activeStructures.entrySet()) {
            if (entry.getValue().tick()) {
                toRemove.add(entry.getKey());
            }
            handler.getStructureData(entry.getValue().structureId).ifPresent(structureEntry -> {
                structureEntry.getCooldownTracker().tick();
                structureEntry.getStructure().ifPresent(structure ->
                        structure.onActiveTick(structureEntry, entry.getValue(), handler.getWorld()));
            });
        }
        for (UUID structId : toRemove) {
            handler.getStructureData(structId).ifPresent(structureEntry ->
                    structureEntry.getStructure().ifPresent(structure ->
                            structure.onStructureDeactivate(structureEntry,
                                    activeStructures.get(structId), handler.getWorld())));
            activeStructures.remove(structId);
        }
    }
}
