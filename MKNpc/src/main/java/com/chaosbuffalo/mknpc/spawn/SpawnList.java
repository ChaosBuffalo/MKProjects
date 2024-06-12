package com.chaosbuffalo.mknpc.spawn;

import com.chaosbuffalo.mknpc.MKNpc;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;

public class SpawnList implements INBTSerializable<CompoundTag> {
    public static final Codec<SpawnList> CODEC = SpawnOption.CODEC.listOf().xmap(SpawnList::new, SpawnList::getOptions);

    private final List<SpawnOption> options;

    private SpawnList(List<SpawnOption> options) {
        this.options = new ArrayList<>(options);
    }

    public SpawnList() {
        options = new ArrayList<>();
    }

    public List<SpawnOption> getOptions() {
        return options;
    }

    public void addOption(SpawnOption option) {
        options.add(option);
    }

    public void copyList(SpawnList other) {
        this.options.clear();
        for (SpawnOption option : other.getOptions()) {
            addOption(option);
        }
    }

    public void setWeightForOption(int index, double weight) {
        options.get(index).setWeight(weight);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag opts = new ListTag();
        for (SpawnOption option : getOptions()) {
            opts.add(option.serialize(NbtOps.INSTANCE));
        }
        tag.put("options", opts);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ListTag opts = nbt.getList("options", Tag.TAG_COMPOUND);
        options.clear();
        for (int i = 0; i < opts.size(); i++) {
            CompoundTag option = opts.getCompound(i);
            SpawnOption spawnOption = SpawnOption.deserialize(option);
            addOption(spawnOption);
        }
    }

    public <D> D serialize(DynamicOps<D> ops) {
        return CODEC.encodeStart(ops, this).getOrThrow(false, MKNpc.LOGGER::error);
    }

    public static SpawnList deserialize(Tag tag) {
        return CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow(false, MKNpc.LOGGER::error);
    }
}
