package com.chaosbuffalo.mknpc.spawn;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SpawnList implements INBTSerializable<CompoundTag> {

    public static final Codec<SpawnList> CODEC = RecordCodecBuilder.<SpawnList>mapCodec(builder -> {
        return builder.group(
               SpawnOption.CODEC.listOf().fieldOf("options").forGetter(SpawnList::getOptions)
        ).apply(builder, SpawnList::new);
    }).codec();

    private final List<SpawnOption> options;

    public SpawnList() {
        this.options = new ArrayList<>();
    }

    public SpawnList(List<SpawnOption> options) {
        this.options = new ArrayList<>(options);
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
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag opts = new ListTag();
        for (SpawnOption option : getOptions()) {
            opts.add(option.serializeNBT(provider));
        }
        tag.put("options", opts);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        ListTag opts = nbt.getList("options", Tag.TAG_COMPOUND);
        options.clear();
        for (int i = 0; i < opts.size(); i++) {
            CompoundTag option = opts.getCompound(i);
            SpawnOption spawnOption = new SpawnOption();
            spawnOption.deserializeNBT(provider, option);
            addOption(spawnOption);
        }
    }
}
