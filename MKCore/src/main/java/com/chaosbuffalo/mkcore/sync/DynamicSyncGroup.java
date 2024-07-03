package com.chaosbuffalo.mkcore.sync;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import java.util.Collection;
import java.util.function.Supplier;

public abstract class DynamicSyncGroup extends NamedSyncGroup {
    protected static final String FULL_FLAG = "#f";
    private boolean forceFull;

    public DynamicSyncGroup(String name) {
        super(name);
    }

    @Override
    public boolean isDirty() {
        return forceFull || super.isDirty();
    }

    @Override
    public void add(ISyncObject sync) {
        add(sync, true);
    }

    public void add(ISyncObject sync, boolean setDirty) {
        super.add(sync);
        if (setDirty) {
            childUpdated(sync);
        }
    }

    @Override
    public void remove(ISyncObject syncObject) {
        super.remove(syncObject);
        forceFull = true;
        scheduleUpdate();
    }

    @Override
    protected void readComponentUpdates(HolderLookup.Provider provider, CompoundTag groupTag) {
        boolean fullSync = groupTag.contains(FULL_FLAG);
        beforeClientUpdate(groupTag, fullSync);
        super.readComponentUpdates(provider, groupTag);
        afterClientUpdate(groupTag, fullSync);
    }

    @Override
    protected void writeComponentUpdates(HolderLookup.Provider provider, CompoundTag groupTag, Collection<ISyncObject> objects) {
        if (forceFull) {
            writeFullComponents(provider, groupTag, components);
            forceFull = false;
        } else {
            super.writeComponentUpdates(provider, groupTag, objects);
        }
    }

    @Override
    protected void writeFullComponents(HolderLookup.Provider provider, CompoundTag groupTag, Collection<ISyncObject> objects) {
        groupTag.putBoolean(FULL_FLAG, true);
        super.writeFullComponents(provider, groupTag, objects);
    }

    protected abstract void preUpdateEntry(String key, Supplier<CompoundTag> value);

    protected void beforeClientUpdate(CompoundTag groupTag, boolean fullSync) {
        for (String key : groupTag.getAllKeys()) {
            if (key.startsWith("#"))
                continue;
            preUpdateEntry(key, () -> groupTag.getCompound(key));
        }
    }

    protected void afterClientUpdate(CompoundTag groupTag, boolean fullSync) {

    }
}
