package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.AbilityType;
import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import com.chaosbuffalo.mkcore.utils.MKNBTUtil;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.DynamicLike;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;


public class MKAbilityInfo implements IMKSerializable<CompoundTag> {

    private final ResourceLocation abilityId;
    private final MKAbility ability;
    private final Set<AbilitySource> sources = new HashSet<>(2);
    @Nullable
    private AbilitySource highestSource;

    public MKAbilityInfo(MKAbility ability) {
        this.ability = ability;
        abilityId = ability.getAbilityId();
    }

    public MKAbilityInfo(ResourceLocation abilityId, MKAbility ability) {
        this.ability = ability;
        this.abilityId = abilityId;
    }

    @Nonnull
    public MKAbility getAbility() {
        return ability;
    }

    public AbilityType getAbilityType() {
        return ability.getType();
    }

    public MutableComponent getAbilityName() {
        return ability.getAbilityName();
    }

    public ResourceLocation getAbilityIcon() {
        return ability.getAbilityIcon();
    }

    public ResourceLocation getId() {
        return abilityId;
    }

    public boolean isCurrentlyKnown() {
        return sources.size() > 0;
    }

    public Set<AbilitySource> getSources() {
        return sources;
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

    public MKAbilityInfo copy() {
        return new MKAbilityInfo(abilityId, ability);
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

    public void write(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(getId());
        buffer.writeRegistryIdUnsafe(MKCoreRegistry.ABILITIES, ability);
        buffer.writeNbt(serialize());
    }

    public static MKAbilityInfo read(FriendlyByteBuf buffer) {
        ResourceLocation id = buffer.readResourceLocation();
        MKAbility abilityType = buffer.readRegistryIdUnsafe(MKCoreRegistry.ABILITIES);
        if (abilityType == null)
            return null;
        CompoundTag data = buffer.readNbt();

        MKAbilityInfo info = abilityType.createAbilityInfo(id);
        if (data != null) {
            info.deserialize(data);
        }

        return info;
    }

    public <D> D serialize(DynamicOps<D> ops) {
        ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
        builder.put(ops.createString("id"), ops.createString(getId().toString()));
        builder.put(ops.createString("type"), ops.createString(ability.getAbilityId().toString()));
        builder.put(ops.createString("data"), NbtOps.INSTANCE.convertTo(ops, serialize()));
        return ops.createMap(builder.build());
    }

    public static <D> MKAbilityInfo deserialize(DynamicLike<D> dynamic) {
        ResourceLocation id = dynamic.get("id").asString().map(ResourceLocation::tryParse).getOrThrow(false, MKCore.LOGGER::error);
        ResourceLocation abilityTypeId = dynamic.get("type").asString().map(ResourceLocation::tryParse).getOrThrow(false, MKCore.LOGGER::error);

        MKAbility abilityType = MKCoreRegistry.getAbility(abilityTypeId);
        if (abilityType == null) {
            return null;
        }

        MKAbilityInfo info = abilityType.createAbilityInfo(id);

        dynamic.get("data").result().map(d -> d.convert(NbtOps.INSTANCE).getValue()).ifPresent(tag -> {
            if (tag instanceof CompoundTag compoundTag) {
                info.deserialize(compoundTag);
            }
        });
        return info;
    }

    @Nullable
    public static MKAbilityInfo fromTag(ResourceLocation abilityId, CompoundTag tag) {
        ResourceLocation abilityTypeId = MKNBTUtil.readResourceLocation(tag, "type");
        MKAbility ability = MKCoreRegistry.getAbility(abilityTypeId);
        if (ability == null) {
            return null;
        }
        MKAbilityInfo abilityInfo = ability.createAbilityInfo(abilityId);
        abilityInfo.deserialize(tag);
        return abilityInfo;
    }

    @Override
    public String toString() {
        return "MKAbilityInfo{" +
                "ability=" + ability +
                ", sources=" + sources.size() +
                ", highestSource=" + highestSource +
                '}';
    }
}
