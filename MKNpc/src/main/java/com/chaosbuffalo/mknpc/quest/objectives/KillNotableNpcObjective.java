package com.chaosbuffalo.mknpc.quest.objectives;

import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.capabilities.NpcCapabilities;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.npc.NotableNpcEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.quest.QuestStructureLocation;
import com.chaosbuffalo.mknpc.quest.data.QuestData;
import com.chaosbuffalo.mknpc.quest.data.objective.UUIDInstanceData;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestChainInstance;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestObjectiveData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class KillNotableNpcObjective extends QuestObjective<UUIDInstanceData> implements IKillObjectiveHandler {
    public static final Codec<KillNotableNpcObjective> CODEC = RecordCodecBuilder.<KillNotableNpcObjective>mapCodec(builder -> {
        return builder.group(
                Codec.STRING.fieldOf("objectiveName").forGetter(i -> i.objectiveName),
                QuestStructureLocation.CODEC.fieldOf("structure").forGetter(i -> i.location),
                ResourceLocation.CODEC.fieldOf("npcDefinition").forGetter(i -> i.npcDefinition)
        ).apply(builder, KillNotableNpcObjective::new);
    }).codec();

    private final ResourceLocation npcDefinition;

    public KillNotableNpcObjective(String name, QuestStructureLocation structureLocation, ResourceLocation npcDef) {
        super(name, structureLocation);
        npcDefinition = npcDef;
    }

    @Override
    public QuestObjectiveType<? extends QuestObjective<?>> getType() {
        return QuestObjectiveTypes.KILL_NOTABLE_NPC.get();
    }

    @Override
    public List<Component> getDescription() {
        return List.of();
    }

    @Override
    public boolean onPlayerKillNpcDefEntity(Player player, PlayerQuestObjectiveData objectiveData, NpcDefinition def,
                                            LivingDeathEvent event, QuestData quest, PlayerQuestChainInstance playerChain) {
        if (!isComplete(objectiveData)) {
            UUIDInstanceData objData = getInstanceData(quest);
            boolean applies = event.getEntity().getCapability(NpcCapabilities.ENTITY_NPC_DATA_CAPABILITY)
                    .map(x -> x.getNotableUUID().equals(objData.getUUID()))
                    .orElse(false);
            if (applies) {
                objectiveData.putBool("hasKilled", true);
                objectiveData.removeBlockPos("npcPos");
                player.sendSystemMessage(Component.translatable("mknpc.objective.kill_notable.complete",
                        event.getEntity().getDisplayName()).withStyle(ChatFormatting.GOLD));
                signalCompleted(objectiveData);
                playerChain.notifyDirty();
                return true;
            }
        }
        return false;
    }

    @Override
    public UUIDInstanceData generateInstanceData(Map<ResourceLocation, List<MKStructureEntry>> questStructures) {
        MKStructureEntry entry = questStructures.get(location.getStructureId()).get(location.getIndex());
        Optional<NotableNpcEntry> npcOpt = entry.getFirstNotableOfType(npcDefinition);
        return npcOpt.map(x -> new UUIDInstanceData(x.getNotableId())).orElse(new UUIDInstanceData());
    }

    @Override
    public UUIDInstanceData instanceDataFactory() {
        return new UUIDInstanceData();
    }


    @Override
    public boolean isStructureRelevant(MKStructureEntry entry) {
        return location.getStructureId().equals(entry.getStructureName()) && entry.hasNotableOfType(npcDefinition);
    }

    @Override
    public PlayerQuestObjectiveData generatePlayerData(IWorldNpcData worldData, QuestData questData) {
        UUIDInstanceData objData = getInstanceData(questData);
        PlayerQuestObjectiveData newObj = new PlayerQuestObjectiveData(getObjectiveName(), getDescription());
        NotableNpcEntry notable = worldData.getNotableNpc(objData.getUUID());
        if (notable != null) {
            newObj.setDescription(Component.translatable("mknpc.objective.kill_notable.desc", notable.getName()));
            newObj.putBlockPos("npcPos", notable.getLocation());
        }
        newObj.putBool("hasKilled", false);
        return newObj;
    }
}
