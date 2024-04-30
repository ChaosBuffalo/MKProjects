package com.chaosbuffalo.mknpc.quest.objectives;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mknpc.MKNpc;
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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class QuestLootNotableObjective extends QuestObjective<UUIDInstanceData> implements IKillObjectiveHandler {
    public static final Codec<QuestLootNotableObjective> CODEC = RecordCodecBuilder.<QuestLootNotableObjective>mapCodec(builder -> {
        return builder.group(
                Codec.STRING.fieldOf("objectiveName").forGetter(i -> i.objectiveName),
                QuestStructureLocation.CODEC.fieldOf("structure").forGetter(i -> i.location),
                ResourceLocation.CODEC.fieldOf("npcDefinition").forGetter(i -> i.npcDefinition),
                Codec.DOUBLE.optionalFieldOf("chance", 1.0).forGetter(i -> i.chanceToFind),
                Codec.INT.optionalFieldOf("count", 1).forGetter(i -> i.requiredCount),
                ExtraCodecs.COMPONENT.fieldOf("itemDescription").forGetter(i -> i.itemDescription)
        ).apply(builder, QuestLootNotableObjective::new);
    }).codec();


    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "objective.quest_loot_notable");
    protected Component itemDescription;
    private final ResourceLocation npcDefinition;
    private final double chanceToFind;
    private final int requiredCount;

    public QuestLootNotableObjective(String name, QuestStructureLocation structureLocation, ResourceLocation npcDefinition,
                                     double chance, int count, Component itemDescription) {
        super(name, structureLocation);
        this.npcDefinition = npcDefinition;
        chanceToFind = chance;
        requiredCount = count;
        this.itemDescription = itemDescription;
    }

    @Override
    public QuestObjectiveType<? extends QuestObjective<?>> getType() {
        return QuestObjectiveTypes.QUEST_LOOT_NOTABLE.get();
    }

    @Override
    public List<Component> getDescription() {
        return List.of();
    }

    private MutableComponent getDescriptionWithCount(Component name, int count) {
        return Component.translatable("mknpc.objective.quest_loot_npc.desc", itemDescription, name,
                MKAbility.INTEGER_FORMATTER.format(count), MKAbility.INTEGER_FORMATTER.format(requiredCount));
    }

    private MutableComponent getProgressMessage(LivingEntity entity, int count) {
        return Component.translatable("mknpc.objective.quest_loot_npc.progress", itemDescription, entity.getName(),
                MKAbility.INTEGER_FORMATTER.format(count), MKAbility.INTEGER_FORMATTER.format(requiredCount));
    }

    @Override
    public boolean onPlayerKillNpcDefEntity(Player player, PlayerQuestObjectiveData objectiveData, NpcDefinition def,
                                            LivingDeathEvent event, QuestData quest, PlayerQuestChainInstance playerChain) {
        if (!isComplete(objectiveData)) {
            UUIDInstanceData objData = getInstanceData(quest);
            boolean applies = event.getEntity().getCapability(NpcCapabilities.ENTITY_NPC_DATA_CAPABILITY)
                    .map(x -> x.getNotableUUID().equals(objData.getUuid()))
                    .orElse(false);
            if (applies && player.getRandom().nextDouble() <= chanceToFind) {
                int currentCount = objectiveData.getInt("lootCount");
                currentCount++;
                objectiveData.putInt("lootCount", currentCount);
                objectiveData.setDescription(getDescriptionWithCount(event.getEntity().getName(), currentCount));
                player.sendSystemMessage(getProgressMessage(event.getEntity(), currentCount)
                        .withStyle(ChatFormatting.GOLD));
                if (currentCount == requiredCount) {
                    signalCompleted(objectiveData);
                    objectiveData.removeBlockPos("npcPos");
                }
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
        NotableNpcEntry notable = worldData.getNotableNpc(objData.getUuid());
        if (notable != null) {
            newObj.setDescription(getDescriptionWithCount(notable.getName(), 0));
            newObj.putBlockPos("npcPos", notable.getLocation());
        }
        newObj.putInt("lootCount", 0);
        return newObj;
    }
}