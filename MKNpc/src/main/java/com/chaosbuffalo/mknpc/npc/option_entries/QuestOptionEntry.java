package com.chaosbuffalo.mknpc.npc.option_entries;


import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcOptionEntryTypes;
import com.chaosbuffalo.mknpc.npc.entries.QuestOfferingEntry;
import com.chaosbuffalo.mknpc.npc.options.QuestOfferingOption;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestOptionEntry implements INpcOptionEntry {
    public static final Codec<QuestOptionEntry> CODEC = Codec.unboundedMap(ResourceLocation.CODEC, QuestOfferingEntry.CODEC)
            .xmap(QuestOptionEntry::new, i -> i.questOfferings);
    public static final MapCodec<QuestOptionEntry> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.unboundedMap(ResourceLocation.CODEC, QuestOfferingEntry.CODEC).fieldOf("quests").forGetter(i -> i.questOfferings)
    ).apply(builder, QuestOptionEntry::new));

    private final Map<ResourceLocation, QuestOfferingEntry> questOfferings = new HashMap<>();

    private QuestOptionEntry(Map<ResourceLocation, QuestOfferingEntry> map) {
        questOfferings.putAll(map);
    }

    public QuestOptionEntry(List<ResourceLocation> locs) {
        for (ResourceLocation loc : locs) {
            questOfferings.put(loc, new QuestOfferingEntry(loc));
        }
    }

    @Override
    public ResourceLocation getOptionId() {
        return QuestOfferingOption.NAME;
    }

    @Override
    public void applyToEntity(Entity entity) {
//        BlockPos pos = new BlockPos(entity.getPositionVec());
//        for (QuestOfferingEntry entry : questOfferings.values()){
//            if (entry.getQuestId() == null){
//                QuestDefinition definition = QuestDefinitionManager.getDefinition(entry.getQuestDef());
//                if (definition != null) {
//                    MinecraftServer server = entity.getServer();
//                    if (server != null) {
//                        World world = server.getWorld(World.OVERWORLD);
//                        if (world != null) {
//                            Optional<QuestChainInstance> quest = world.getCapability(NpcCapabilities.WORLD_NPC_DATA_CAPABILITY)
//                                    .map(x -> x.buildQuest(definition, pos)).orElse(Optional.empty());
//                            if (quest.isPresent()) {
//                                QuestChainInstance newQuest = quest.get();
//                                MKNpc.getNpcData(entity).ifPresent(x -> newQuest.setQuestSourceNpc(x.getSpawnID()));
//                                entry.setQuestId(newQuest.getQuestId());
//                            }
//                        }
//                    }
//                }
//            }
//        }
        MKNpc.getNpcData(entity).ifPresent(x -> {
            x.putShouldHaveQuest(true);
            for (QuestOfferingEntry entry : questOfferings.values()) {
                x.requestQuest(entry);
            }
        });
    }

    @Override
    public NpcOptionEntryType<? extends INpcOptionEntry> getType() {
        return NpcOptionEntryTypes.QUEST.get();
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        ListTag offeringNbt = new ListTag();
        for (QuestOfferingEntry entry : questOfferings.values()) {
            offeringNbt.add(QuestOfferingEntry.CODEC.encodeStart(NbtOps.INSTANCE, entry).getOrThrow());
        }
        nbt.put("offerings", offeringNbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        ListTag offeringNbt = nbt.getList("offerings", Tag.TAG_COMPOUND);
        for (Tag offering : offeringNbt) {
            QuestOfferingEntry newEntry = QuestOfferingEntry.CODEC.parse(NbtOps.INSTANCE, offering).getOrThrow();
            questOfferings.put(newEntry.getQuestDef(), newEntry);
        }
    }
}
