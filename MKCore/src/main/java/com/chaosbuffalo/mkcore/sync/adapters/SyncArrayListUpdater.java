package com.chaosbuffalo.mkcore.sync.adapters;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.sync.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import com.chaosbuffalo.mkcore.sync.SyncContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.List;
import java.util.function.BiFunction;

public class SyncArrayListUpdater<T> implements ISyncObject {
    private final List<T> parent;
    private final BiFunction<SyncContext, T, Tag> valueEncoder;
    private final BiFunction<SyncContext, Tag, T> valueDecoder;
    private final BitSet dirtyEntries = new BitSet();
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

    public static SyncArrayListUpdater<ResourceLocation> resourceLocations(List<ResourceLocation> list) {
        return new SyncArrayListUpdater<>(list,
                (context, location) -> StringTag.valueOf(location.toString()),
                (context, tag) -> ResourceLocation.tryParse(tag.getAsString()));
    }

    public SyncArrayListUpdater(List<T> list,
                                BiFunction<SyncContext, T, Tag> valueEncoder,
                                BiFunction<SyncContext, Tag, T> valueDecoder) {
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
    public @Nullable Tag writeFullValue(SyncContext context) {
        CompoundTag root = new CompoundTag();
        root.putBoolean("f", true);
        ListTag list = new ListTag();
        parent.forEach(r -> list.add(valueEncoder.apply(context, r)));
        root.put("l", list);
        return root;
    }

    @Override
    public @Nullable Tag writeUpdateValue(SyncContext context) {
        if (dirtyEntries.isEmpty())
            return null;

        CompoundTag root = new CompoundTag();
        ListTag list = new ListTag();
        dirtyEntries.stream().forEach(i -> {
            CompoundTag tag = new CompoundTag();
            tag.putInt("i", i);
            tag.put("v", valueEncoder.apply(context, parent.get(i)));
            list.add(tag);
        });
        root.put("s", list);
        dirtyEntries.clear();
        return root;
    }

    @Override
    public void handleUpdatePayload(SyncContext context, Tag valueTag) {
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
        T decoded = valueDecoder.apply(context, encodedValue);
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
