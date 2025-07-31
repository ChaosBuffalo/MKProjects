package com.chaosbuffalo.mknpc.npc.option_entries;


import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcOptionEntryTypes;
import com.chaosbuffalo.mknpc.npc.entries.QuestOfferingEntry;
import com.chaosbuffalo.mknpc.quest.QuestDefinition;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestOptionEntry implements INpcOptionEntry {
    public static final MapCodec<QuestOptionEntry> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.unboundedMap(QuestDefinition.KEY_CODEC, QuestOfferingEntry.CODEC).fieldOf("quests").forGetter(i -> i.questOfferings)
    ).apply(builder, QuestOptionEntry::new));

    private final Map<ResourceKey<QuestDefinition>, QuestOfferingEntry> questOfferings = new HashMap<>();

    private QuestOptionEntry(Map<ResourceKey<QuestDefinition>, QuestOfferingEntry> map) {
        questOfferings.putAll(map);
    }

    public QuestOptionEntry(List<ResourceKey<QuestDefinition>> locs) {
        for (ResourceKey<QuestDefinition> loc : locs) {
            questOfferings.put(loc, new QuestOfferingEntry(loc));
        }
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
}
