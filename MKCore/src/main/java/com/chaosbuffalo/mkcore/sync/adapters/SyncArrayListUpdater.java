package com.chaosbuffalo.mkcore.sync.adapters;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.sync.SyncContext;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import com.chaosbuffalo.mkcore.sync.v2.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.v2.ISyncObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.List;

public class SyncArrayListUpdater<T> implements ISyncObject {
    private final List<T> parent;
    private final ElementSerializer<T> serializer;
    private final BitSet dirtyEntries = new BitSet();
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

    public interface ElementSerializer<T> {
        Tag encodeValue(SyncContext context, T value);

        T decodeValue(SyncContext context, Tag value);

        ElementSerializer<ResourceLocation> RESOURCE_LOCATION = new ElementSerializer<>() {
            @Override
            public Tag encodeValue(SyncContext context, ResourceLocation value) {
                return StringTag.valueOf(value.toString());
            }

            @Override
            public ResourceLocation decodeValue(SyncContext context, Tag value) {
                return ResourceLocation.tryParse(value.getAsString());
            }
        };
    }

    public static SyncArrayListUpdater<ResourceLocation> resourceLocations(List<ResourceLocation> list) {
        return new SyncArrayListUpdater<>(list, ElementSerializer.RESOURCE_LOCATION);
    }

    public SyncArrayListUpdater(List<T> list, ElementSerializer<T> elementSerializer) {
        this.parent = list;
        this.serializer = elementSerializer;
    }

    public void setDirty(int index) {
        dirtyEntries.set(index);
        parentNotifier.notifyUpdate();
    }

    @Override
    public void setSyncUpdateNotifier(ISyncNotifier notifier) {
        parentNotifier = notifier;
    }

    @Override
    public boolean isDirty() {
        return !dirtyEntries.isEmpty();
    }

    @Override
    public void clearDirty() {
        dirtyEntries.clear();
    }

    @Override
    public @Nullable Tag writeFullValue(SyncContext context, SyncVisibility visibility) {
        if (parent.isEmpty())
            return null;

        CompoundTag root = new CompoundTag();
        root.putBoolean("f", true);
        ListTag list = new ListTag();
        parent.forEach(r -> list.add(serializer.encodeValue(context, r)));
        root.put("l", list);
        return root;
    }

    @Override
    public @Nullable Tag writeDirtyValue(SyncContext context, SyncVisibility visibility) {
        if (dirtyEntries.isEmpty())
            return null;

        CompoundTag root = new CompoundTag();
        ListTag list = new ListTag();
        dirtyEntries.stream().forEach(i -> {
            CompoundTag tag = new CompoundTag();
            tag.putInt("i", i);
            tag.put("v", serializer.encodeValue(context, parent.get(i)));
            list.add(tag);
        });
        root.put("s", list);
        dirtyEntries.clear();
        return root;
    }

    @Override
    public void handleUpdatePayload(SyncContext context, Tag valueTag, SyncVisibility visibility) {
        if (valueTag instanceof CompoundTag root) {
            if (root.getBoolean("f")) {
                parent.clear();
            }

            if (root.contains("s")) {
                var sparseList = root.getList("s", Tag.TAG_COMPOUND);
                for (int i = 0; i < sparseList.size(); i++) {
                    CompoundTag entry = sparseList.getCompound(i);
                    int index = entry.getInt("i");
                    setValueInternal(context, index, entry.get("v"));
                }
            } else if (root.contains("l")) {
                if (root.get("l") instanceof ListTag fullList) {
                    for (int i = 0; i < fullList.size(); i++) {
                        setValueInternal(context, i, fullList.get(i));
                    }
                }
            }
        }
    }

    private void setValueInternal(SyncContext context, int index, Tag encodedValue) {
        T decoded = serializer.decodeValue(context, encodedValue);
        if (decoded != null) {
            if (index < parent.size()) {
                parent.set(index, decoded);
            } else {
                MKCore.LOGGER.error("Failed set update item: Index {} out of range ({} max)", index, parent.size());
            }
        } else {
            MKCore.LOGGER.error("Failed to decode list entry {}: {}", index, encodedValue);
        }
    }
}
