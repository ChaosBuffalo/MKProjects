package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IEntityNpcData;
import com.chaosbuffalo.mknpc.capabilities.WorldStructureManager;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.StructureEvent;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.StructureEventManager;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class MKStructure extends Structure {

    @Nullable
    protected Component enterMessage;
    @Nullable
    protected Component exitMessage;
    protected final Map<String, StructureEvent> events = new HashMap<>();

    protected MKStructure(StructureSettings pSettings, CompoundTag structureNbt) {
        super(pSettings);
        loadNbt(structureNbt);
    }

    @Nullable
    protected Tag serializeEvents() {
        if (events.isEmpty()) {
            return null;
        }
        ListTag structureEvents = new ListTag();
        for (StructureEvent event : events.values()) {
            structureEvents.add(event.serialize(NbtOps.INSTANCE));
        }
        return structureEvents;
    }

    protected void deserializeEvents(Tag tag) {
        if (!(tag instanceof ListTag evList))
            return;

        for (int i = 0; i < evList.size(); i++) {
            CompoundTag evTag = evList.getCompound(i);
            Dynamic<?> dyn = new Dynamic<>(NbtOps.INSTANCE, evTag);
            ResourceLocation type = StructureEvent.getType(dyn);
            Supplier<StructureEvent> supplier = StructureEventManager.getEventDeserializer(type);
            if (supplier != null) {
                StructureEvent ev = supplier.get();
                ev.deserialize(dyn);
                addEvent(ev.getEventName(), ev);
            }
        }
    }

    public CompoundTag getNbt() {
        CompoundTag tag = new CompoundTag();
        Tag events = serializeEvents();
        if (events != null) {
            tag.put("structure_events", events);
        }
        return tag;
    }

    public void loadNbt(CompoundTag tag) {
        deserializeEvents(tag.get("structure_events"));
    }

    public MKStructure addEvent(String name, StructureEvent event) {
        event.setEventName(name);
        events.put(name, event);
        return this;
    }

    public MKStructure setEnterMessage(Component msg) {
        this.enterMessage = msg;
        return this;
    }

    @Nullable
    public Component getEnterMessage() {
        return enterMessage;
    }

    public MKStructure setExitMessage(Component msg) {
        this.exitMessage = msg;
        return this;
    }

    @Nullable
    public Component getExitMessage() {
        return exitMessage;
    }

    public void onPlayerEnter(ServerPlayer player, MKStructureEntry structureEntry,
                              WorldStructureManager.ActiveStructure activeStructure) {
        if (getEnterMessage() != null) {
            player.sendSystemMessage(getEnterMessage());
        }
    }

    public void onPlayerExit(ServerPlayer player, MKStructureEntry structureEntry,
                             WorldStructureManager.ActiveStructure activeStructure) {
        if (getExitMessage() != null) {
            player.sendSystemMessage(getExitMessage());
        }
    }

    public void onStructureActivate(MKStructureEntry entry, WorldStructureManager.ActiveStructure activeStructure, Level world) {
        MKNpc.LOGGER.debug("Activating structure {} (ID: {})", entry.getStructureName(), entry.getStructureId());
        for (Map.Entry<String, StructureEvent> ev : events.entrySet()) {
            if (ev.getValue().meetsRequirements(entry, activeStructure, world)) {
                entry.addActiveEvent(ev.getKey());
            }
        }
        for (String key : entry.getActiveEvents()) {
            StructureEvent ev = events.get(key);
            if (ev != null && ev.canTrigger(StructureEvent.EventTrigger.ON_ACTIVATE)) {
                checkAndExecuteEvent(ev, entry, activeStructure, world);
            }
        }
    }

    public void onStructureDeactivate(MKStructureEntry entry, WorldStructureManager.ActiveStructure activeStructure, Level world) {
        MKNpc.LOGGER.debug("Deactivating structure {} (ID: {})", entry.getStructureName(), entry.getStructureId());
        for (String key : entry.getActiveEvents()) {
            StructureEvent ev = events.get(key);
            if (ev != null && ev.canTrigger(StructureEvent.EventTrigger.ON_DEACTIVATE)) {
                checkAndExecuteEvent(ev, entry, activeStructure, world);
            }
        }
        entry.clearActiveEvents();
    }

    public void onActiveTick(MKStructureEntry entry, WorldStructureManager.ActiveStructure activeStructure, Level world) {
        for (String key : entry.getActiveEvents()) {
            StructureEvent ev = events.get(key);
            if (ev != null && ev.canTrigger(StructureEvent.EventTrigger.ON_TICK)) {
                checkAndExecuteEvent(ev, entry, activeStructure, world);
            }
        }
    }

    protected void checkAndExecuteEvent(StructureEvent ev, MKStructureEntry entry,
                                        WorldStructureManager.ActiveStructure activeStructure, Level world) {
        if (!entry.getCooldownTracker().hasTimer(ev.getTimerName()) && ev.meetsConditions(entry, activeStructure, world)) {
            ev.execute(entry, activeStructure, world);
            if (ev.startsCooldownImmediately()) {
                entry.getCooldownTracker().setTimer(ev.getTimerName(), ev.getCooldown());
            }

        }
    }

    public void onTrackedEntityDeath(MKStructureEntry entry, WorldStructureManager.ActiveStructure activeStructure, IEntityNpcData npcData,
                                     String eventName) {
        StructureEvent ev = events.get(eventName);
        if (ev != null) {
            ev.onTrackedEntityDeath(entry, activeStructure, npcData);
        }
    }

    public void onNpcDeath(MKStructureEntry entry, WorldStructureManager.ActiveStructure activeStructure, IEntityNpcData npcData) {
        for (String key : entry.getActiveEvents()) {
            StructureEvent ev = events.get(key);
            if (ev != null && ev.canTrigger(StructureEvent.EventTrigger.ON_DEATH)) {
                checkAndExecuteEvent(ev, entry, activeStructure, npcData.getEntity().getCommandSenderWorld());
            }
        }
    }
}
