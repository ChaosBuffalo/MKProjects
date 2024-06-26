package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class PlayerKnownAbility implements IMKSerializable<CompoundTag> {
    public final MKAbilityInfo abilityInfo;
    private final Set<AbilitySource> sources = new HashSet<>(2);
    @Nullable
    private AbilitySource highestSource;

    public PlayerKnownAbility(MKAbilityInfo abilityInfo) {
        this.abilityInfo = abilityInfo;
    }

    public MKAbilityInfo getAbilityInfo() {
        return abilityInfo;
    }

    public ResourceLocation getId() {
        return abilityInfo.getId();
    }

    public Set<AbilitySource> getSources() {
        return sources;
    }

    public boolean isCurrentlyKnown() {
        return !sources.isEmpty();
    }

    public boolean hasSource(AbilitySource source) {
        return sources.contains(source);
    }

    public void addSource(AbilitySource source) {
        if (sources.add(source)) {
            updateHighestSource();
        }
    }

    public void removeSource(AbilitySource source) {
        if (sources.remove(source)) {
            updateHighestSource();
        }
    }

    private void updateHighestSource() {
        highestSource = sources.stream()
                .max(Comparator.comparingInt(s -> s.getSourceType().getPriority()))
                .orElse(null);
    }

    public boolean usesAbilityPool() {
        return highestSource != null && highestSource.usesAbilityPool();
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();

        ListTag list = new ListTag();
        sources.forEach(s -> list.add(s.serialize()));
        tag.put("sources", list);
        return tag;
    }

    @Override
    public CompoundTag serializeStorage() {
        CompoundTag tag = new CompoundTag();

        ListTag list = new ListTag();
        sources.forEach(s -> {
            // Only store sources that will not be reapplied on login
            if (s.getSourceType().isPersistent()) {
                list.add(s.serialize());
            }
        });
        tag.put("sources", list);
        return tag;
    }

    @Override
    public boolean deserialize(CompoundTag tag) {
        sources.clear();
        if (tag.contains("sources")) {
            ListTag list = tag.getList("sources", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                AbilitySource source = AbilitySource.deserialize(list.getString(i));
                if (source == null) {
                    MKCore.LOGGER.error("Failed to decode AbilitySource {}", list.getString(i));
                    return false;
                }
                addSource(source);
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "PlayerKnownAbility{" +
                "ability=" + abilityInfo +
                ", sources=" + sources.size() +
                ", highestSource=" + highestSource +
                '}';
    }
}
