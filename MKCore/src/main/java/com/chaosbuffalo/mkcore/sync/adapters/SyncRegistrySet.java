package com.chaosbuffalo.mkcore.sync.adapters;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.sync.SyncContext;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import com.chaosbuffalo.mkcore.sync.v2.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.v2.ISyncObject;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class SyncRegistrySet<K, V> implements ISyncObject {
    private final Set<K> backingSet;
    private final ResourceKey<? extends Registry<V>> registryKey;
    private final RegistryElementAdapter<K, V> adapter;
    private final Set<K> dirty = new HashSet<>();
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;
    @Nullable
    private Consumer<Set<K>> onChangedCallback;

    public SyncRegistrySet(Set<K> backingSet,
                           ResourceKey<? extends Registry<V>> registryKey,
                           RegistryElementAdapter<K, V> adapter) {
        this.backingSet = backingSet;
        this.registryKey = registryKey;
        this.adapter = adapter;
    }

    public static <V> SyncRegistrySet<ResourceKey<V>, V> resourceKeys(
            Set<ResourceKey<V>> backingSet,
            ResourceKey<? extends Registry<V>> registryKey) {
        return new SyncRegistrySet<>(backingSet, registryKey, RegistryElementAdapter.resourceKeys());
    }

    public static <V> SyncRegistrySet<ResourceLocation, V> resourceLocations(
            Set<ResourceLocation> backingSet,
            ResourceKey<? extends Registry<V>> registryKey) {
        return new SyncRegistrySet<>(backingSet, registryKey, RegistryElementAdapter.resourceLocations());
    }

    public void setOnChangedCallback(Consumer<Set<K>> onChangedCallback) {
        this.onChangedCallback = onChangedCallback;
    }

    public boolean add(K element) {
        if (!trackLocalAdd(element)) {
            return false;
        }
        onLocalChanged();
        return true;
    }

    public boolean remove(K element) {
        if (!trackLocalRemove(element)) {
            return false;
        }
        onLocalChanged();
        return true;
    }

    public boolean contains(K element) {
        return backingSet.contains(element);
    }

    public boolean clear() {
        if (backingSet.isEmpty()) {
            return false;
        }
        for (K element : new HashSet<>(backingSet)) {
            trackLocalRemove(element);
        }
        onLocalChanged();
        return true;
    }

    public boolean replaceAll(Collection<K> newValues) {
        boolean changed = false;
        Set<K> desired = new HashSet<>(newValues);
        for (K existing : new HashSet<>(backingSet)) {
            if (!desired.contains(existing)) {
                changed |= trackLocalRemove(existing);
            }
        }
        for (K desiredValue : desired) {
            changed |= trackLocalAdd(desiredValue);
        }
        if (changed) {
            onLocalChanged();
        }
        return changed;
    }

    public Set<K> view() {
        return Collections.unmodifiableSet(backingSet);
    }

    @Override
    public void setSyncUpdateNotifier(ISyncNotifier notifier) {
        parentNotifier = notifier;
    }

    @Override
    public boolean isDirty() {
        return !dirty.isEmpty();
    }

    @Override
    public void clearDirty() {
        dirty.clear();
    }

    @Override
    public @Nullable Tag writeFullValue(SyncContext context, SyncVisibility visibility) {
        CompoundTag root = new CompoundTag();
        root.putBoolean("f", true);
        root.putIntArray("v", encodeElements(context, backingSet));
        return root;
    }

    @Override
    public @Nullable Tag writeDirtyValue(SyncContext context, SyncVisibility visibility) {
        if (!isDirty()) {
            return null;
        }

        List<K> adds = new ArrayList<>();
        List<K> removes = new ArrayList<>();
        for (K key : dirty) {
            if (backingSet.contains(key)) {
                adds.add(key);
            } else {
                removes.add(key);
            }
        }
        dirty.clear();

        CompoundTag root = new CompoundTag();
        if (!adds.isEmpty()) {
            root.putIntArray("a", encodeElements(context, adds));
        }
        if (!removes.isEmpty()) {
            root.putIntArray("r", encodeElements(context, removes));
        }
        return root.isEmpty() ? null : root;
    }

    @Override
    public void handleUpdatePayload(SyncContext context, Tag valueTag, SyncVisibility visibility) {
        if (!(valueTag instanceof CompoundTag root)) {
            return;
        }

        Registry<V> registry = context.registryOrThrow(registryKey);
        boolean changed = false;
        if (root.getBoolean("f")) {
            changed |= !backingSet.isEmpty();
            backingSet.clear();
        }
        if (root.contains("r", Tag.TAG_INT_ARRAY)) {
            changed |= decodeIds(registry, root.getIntArray("r"), false);
        }
        if (root.contains("a", Tag.TAG_INT_ARRAY)) {
            changed |= decodeIds(registry, root.getIntArray("a"), true);
        }
        if (root.contains("v", Tag.TAG_INT_ARRAY)) {
            changed |= decodeIds(registry, root.getIntArray("v"), true);
        }
        if (changed) {
            fireOnChanged();
        }
    }

    private int[] encodeElements(SyncContext context, Collection<K> elements) {
        Registry<V> registry = context.registryOrThrow(registryKey);
        int[] output = new int[elements.size()];
        int count = 0;
        for (K element : elements) {
            V registryValue = adapter.toRegistryValue(registry, element);
            if (registryValue == null) {
                MKCore.LOGGER.warn("Failed to resolve sync set element {} in registry {}", element, registryKey.location());
                continue;
            }
            int rawId = registry.getId(registryValue);
            if (rawId < 0) {
                MKCore.LOGGER.warn("Failed to encode sync set element {} in registry {}", element, registryKey.location());
                continue;
            }
            output[count++] = rawId;
        }
        return count == output.length ? output : Arrays.copyOf(output, count);
    }

    private boolean decodeIds(Registry<V> registry, int[] ids, boolean add) {
        boolean changed = false;
        for (int rawId : ids) {
            V registryValue = registry.byId(rawId);
            if (registryValue == null) {
                MKCore.LOGGER.warn("Failed to decode sync set raw id {} in registry {}", rawId, registryKey.location());
                continue;
            }
            K decoded = adapter.fromRegistryValue(registry, registryValue);
            if (decoded == null) {
                MKCore.LOGGER.warn("Failed to convert sync set value {} from registry {}", registryValue, registryKey.location());
                continue;
            }
            if (add) {
                changed |= backingSet.add(decoded);
            } else {
                changed |= backingSet.remove(decoded);
            }
        }
        return changed;
    }

    private boolean trackLocalAdd(K element) {
        if (!backingSet.add(element)) {
            return false;
        }
        dirty.add(element);
        return true;
    }

    private boolean trackLocalRemove(K element) {
        if (!backingSet.remove(element)) {
            return false;
        }
        dirty.add(element);
        return true;
    }

    private void onLocalChanged() {
        parentNotifier.notifyUpdate();
        fireOnChanged();
    }

    private void fireOnChanged() {
        if (onChangedCallback != null) {
            onChangedCallback.accept(view());
        }
    }

    @Override
    public String toString() {
        return "SyncRegistrySet[" +
                "registry=" + registryKey.location() +
                ", size=" + backingSet.size() +
                ", dirty=" + dirty.size() +
                ']';
    }
}
