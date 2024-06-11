package com.chaosbuffalo.mknpc.npc.options.binding;


import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.entries.QuestOfferingEntry;
import com.chaosbuffalo.mknpc.npc.options.QuestOfferingOption;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestOfferingBoundValue implements IBoundNpcOptionValue {
    public static final Codec<QuestOfferingBoundValue> CODEC = Codec.unboundedMap(ResourceLocation.CODEC, QuestOfferingEntry.CODEC)
            .xmap(QuestOfferingBoundValue::new, i -> i.questOfferings);

    private final Map<ResourceLocation, QuestOfferingEntry> questOfferings = new HashMap<>();

    private QuestOfferingBoundValue(Map<ResourceLocation, QuestOfferingEntry> map) {
        questOfferings.putAll(map);
    }

    public QuestOfferingBoundValue(List<ResourceLocation> locs) {
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
}
