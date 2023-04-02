package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.sync.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.*;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;

public class AbilityTracker implements ISyncObject {

    private int ticks;
    private final Map<ResourceLocation, TimerEntry> timers = new HashMap<>();

    public boolean hasTimer(ResourceLocation timerId) {
        return getTimerTicksRemaining(timerId) > 0;
    }

    public float getTimerProgressPercent(ResourceLocation timerId, float partialTicks) {
        TimerEntry timer = timers.get(timerId);

        if (timer != null) {
            float totalCooldown = timer.getMaxTicks();
            float currentCooldown = (float) timer.expireTicks - ((float) this.ticks + partialTicks);
            return Mth.clamp(currentCooldown / totalCooldown, 0.0F, 1.0F);
        } else {
            return 0.0F;
        }
    }

    public int getTimerTicksRemaining(ResourceLocation timerId) {
        TimerEntry timer = timers.get(timerId);
        return timer != null ? timer.getRemainingTicks(ticks) : 0;
    }

    public int getTimerMaxTicks(ResourceLocation timerId) {
        TimerEntry timer = timers.get(timerId);
        return timer != null ? timer.getMaxTicks() : 0;
    }

    public void tick() {
        ticks++;

        if (timers.isEmpty())
            return;

        Iterator<Map.Entry<ResourceLocation, TimerEntry>> iterator = timers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ResourceLocation, TimerEntry> entry = iterator.next();
            TimerEntry timerEntry = entry.getValue();
            if (timerEntry.isExpired(ticks)) {
                iterator.remove();
                onTimerRemoved(entry.getKey(), timerEntry.isLocal());
            }
        }
    }

    public void setTimer(ResourceLocation timerId, int ticks) {
        setTimer(timerId, ticks, false);
    }

    public void setLocalTimer(ResourceLocation timerId, int ticks) {
        setTimer(timerId, ticks, true);
    }

    private void setTimer(ResourceLocation timerId, int timerTicks, boolean local) {
        if (timerTicks > 0) {
            timers.put(timerId, new TimerEntry(ticks, ticks + timerTicks, local));
            onTimerAdded(timerId, timerTicks, local);
        } else {
            removeTimer(timerId);
        }
    }

    public void removeTimer(ResourceLocation timerId) {
        TimerEntry entry = timers.remove(timerId);
        if (entry != null) {
            onTimerRemoved(timerId, entry.isLocal());
        }
    }

    protected void onTimerAdded(ResourceLocation timerId, int ticksIn, boolean local) {
    }

    protected void onTimerRemoved(ResourceLocation timerId, boolean local) {
    }

    public CompoundTag serialize() {
        CompoundTag root = new CompoundTag();
        CompoundTag sync = new CompoundTag();
        CompoundTag local = new CompoundTag();

        iterateActiveEntries(e -> true, (entry, remaining) -> {
            ResourceLocation timerId = entry.getKey();
            if (entry.getValue().isLocal()) {
                local.putInt(timerId.toString(), remaining);
            } else {
                sync.putInt(timerId.toString(), remaining);
            }
        });

        if (!sync.isEmpty()) {
            root.put("sync", sync);
        }

        if (!local.isEmpty()) {
            root.put("local", local);
        }
        return root;
    }

    public void deserialize(CompoundTag root) {
        if (root.contains("sync")) {
            deserializeList(root.getCompound("sync"), false);
        }
        if (root.contains("local")) {
            deserializeList(root.getCompound("local"), true);
        }
    }

    private void deserializeList(CompoundTag root, boolean local) {
        for (String key : root.getAllKeys()) {
            setTimer(new ResourceLocation(key), root.getInt(key), local);
        }
    }

    public void iterateActive(ObjIntConsumer<ResourceLocation> consumer) {
        for (ResourceLocation id : timers.keySet()) {
            int cd = getTimerTicksRemaining(id);
            if (cd > 0) {
                consumer.accept(id, cd);
            }
        }
    }

    protected void iterateActiveEntries(Predicate<Map.Entry<ResourceLocation, TimerEntry>> filter,
                                        ObjIntConsumer<Map.Entry<ResourceLocation, TimerEntry>> consumer) {
        timers.entrySet().stream()
                .filter(filter)
                .forEach(e -> {
                    int cd = getTimerTicksRemaining(e.getKey());
                    if (cd > 0) {
                        consumer.accept(e, cd);
                    }
                });
    }

    public void removeAll() {
        Iterator<Map.Entry<ResourceLocation, TimerEntry>> iterator = timers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ResourceLocation, TimerEntry> entry = iterator.next();
            iterator.remove();
            onTimerRemoved(entry.getKey(), entry.getValue().isLocal());
        }
    }

    private static class TimerEntry {
        private final int createTicks;
        private final int expireTicks;
        private final boolean local;

        private TimerEntry(int startTime, int expiration, boolean local) {
            createTicks = startTime;
            expireTicks = expiration;
            this.local = local;
        }

        public boolean isLocal() {
            return local;
        }

        public int getRemainingTicks(int currentTicks) {
            return Math.max(0, expireTicks - currentTicks);
        }

        public int getMaxTicks() {
            return Math.max(0, expireTicks - createTicks);
        }

        public boolean isExpired(int currentTicks) {
            return expireTicks <= currentTicks;
        }
    }

    static class AbilityTrackerServer extends AbilityTracker {

        private final List<ResourceLocation> dirty = new ArrayList<>();
        private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

        public AbilityTrackerServer() {
        }

        @Override
        protected void onTimerAdded(ResourceLocation timerId, int ticksIn, boolean local) {
            super.onTimerAdded(timerId, ticksIn, local);
            markDirty(timerId, local);
        }

        @Override
        protected void onTimerRemoved(ResourceLocation timerId, boolean local) {
            super.onTimerRemoved(timerId, local);
            markDirty(timerId, local);
        }

        @Override
        public void setNotifier(ISyncNotifier notifier) {
            parentNotifier = notifier;
        }

        private void markDirty(ResourceLocation timerId, boolean local) {
            if (!local) {
                dirty.add(timerId);
                parentNotifier.notifyUpdate(this);
            }
        }

        @Override
        public boolean isDirty() {
            return dirty.size() > 0;
        }

        @Override
        public void serializeUpdate(CompoundTag tag) {
            CompoundTag root = new CompoundTag();
            dirty.forEach(id -> root.putInt(id.toString(), getTimerTicksRemaining(id)));
            tag.put("cooldowns", root);
            dirty.clear();
        }

        @Override
        public void serializeFull(CompoundTag tag) {
            CompoundTag root = new CompoundTag();
            iterateActiveEntries(e -> {
                return !e.getValue().isLocal();
            }, (entry, cd) -> {
                root.putInt(entry.getKey().toString(), cd);
            });
            tag.put("cooldowns", root);
            dirty.clear();
        }
    }


    public static AbilityTracker getTracker(LivingEntity entity) {
        if (entity instanceof ServerPlayer) {
            return new AbilityTrackerServer();
        } else {
            return new AbilityTracker();
        }
    }

    @Override
    public void setNotifier(ISyncNotifier notifier) {

    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public void deserializeUpdate(CompoundTag tag) {
        deserializeList(tag.getCompound("cooldowns"), false);
    }

    @Override
    public void serializeUpdate(CompoundTag tag) {

    }

    @Override
    public void serializeFull(CompoundTag tag) {

    }
}
