package com.chaosbuffalo.mkcore.sync;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.BitSet;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class SyncListUpdater<T> implements ISyncObject {
    private final Supplier<List<T>> parent;
    private final String name;
    private final Function<T, Tag> valueEncoder;
    private final Function<Tag, T> valueDecoder;
    private final BitSet dirtyEntries = new BitSet();
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

    public SyncListUpdater(String name, Supplier<List<T>> list, Function<T, Tag> valueEncoder, Function<Tag, T> valueDecoder) {
        this.name = name;
        this.parent = list;
        this.valueDecoder = valueDecoder;
        this.valueEncoder = valueEncoder;
    }

    public void setDirty(int index) {
        dirtyEntries.set(index);
        parentNotifier.notifyUpdate(this);
    }

    @Override
    public void setNotifier(ISyncNotifier notifier) {
        parentNotifier = notifier;
    }

    @Override
    public boolean isDirty() {
        return !dirtyEntries.isEmpty();
    }

    @Override
    public void deserializeUpdate(CompoundTag tag) {
        CompoundTag root = tag.getCompound(name);

        if (root.getBoolean("f")) {
            parent.get().clear();
        }

        if (root.contains("s")) {
            deserializeSparseListUpdate(root.getList("s", Tag.TAG_COMPOUND));
        } else if (root.contains("l")) {
            deserializeStorage(root.get("l"));
        }
    }

    @Override
    public void serializeUpdate(CompoundTag tag) {
        if (dirtyEntries.isEmpty())
            return;

        CompoundTag root = new CompoundTag();
        root.put("s", serializeSparseList(dirtyEntries));
        tag.put(name, root);
        dirtyEntries.clear();
    }

    @Override
    public void serializeFull(CompoundTag tag) {
        CompoundTag root = new CompoundTag();

        root.putBoolean("f", true);
        root.put("l", serializeStorage());
        tag.put(name, root);
        dirtyEntries.clear();
    }

    private CompoundTag makeSparseEntry(int index, T value) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("i", index);
        tag.put("v", valueEncoder.apply(value));
        return tag;
    }

    private ListTag serializeSparseList(BitSet dirtyEntries) {
        List<T> fullList = parent.get();
        ListTag list = new ListTag();
        dirtyEntries.stream().forEach(i -> list.add(makeSparseEntry(i, fullList.get(i))));
        return list;
    }

    private void deserializeFullList(ListTag list) {
        for (int i = 0; i < list.size(); i++) {
            setValueInternal(i, list.get(i));
        }
    }

    private void deserializeSparseListUpdate(ListTag list) {
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int index = entry.getInt("i");
            setValueInternal(index, entry.get("v"));
        }
    }

    private void setValueInternal(int index, Tag encodedValue) {
        T decoded = valueDecoder.apply(encodedValue);
        List<T> list = parent.get();
        if (decoded != null) {
            if (index < list.size()) {
                list.set(index, decoded);
            } else {
                MKCore.LOGGER.error("Failed set update item: Index {} out of range ({} max)", index, list.size());
            }
        } else {
            MKCore.LOGGER.error("Failed to decode list entry {}: {}", index, encodedValue);
        }
    }

    public Tag serializeStorage() {
        ListTag list = new ListTag();
        parent.get().forEach(r -> list.add(valueEncoder.apply(r)));
        return list;
    }

    public void deserializeStorage(Tag tag) {
        if (tag instanceof ListTag) {
            ListTag list = (ListTag) tag;
            deserializeFullList(list);
        }
    }
}
