package com.chaosbuffalo.mknpc.quest.objectives;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.capabilities.NpcCapabilities;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcDefinitionManager;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuestLootNpcObjective extends QuestObjective<UUIDInstanceData> implements IKillObjectiveHandler {
    public static final Codec<QuestLootNpcObjective> CODEC = RecordCodecBuilder.<QuestLootNpcObjective>mapCodec(builder -> {
        return builder.group(
                Codec.STRING.fieldOf("objectiveName").forGetter(i -> i.objectiveName),
                QuestStructureLocation.CODEC.fieldOf("structure").forGetter(i -> i.location),
                ResourceLocation.CODEC.fieldOf("npcDefinition").forGetter(i -> i.npcDefinition),
                Codec.DOUBLE.optionalFieldOf("chance", 1.0).forGetter(i -> i.chanceToFind),
                Codec.INT.optionalFieldOf("count", 1).forGetter(i -> i.requiredCount),
                ExtraCodecs.COMPONENT.fieldOf("itemDescription").forGetter(i -> i.itemDescription),
                Codec.list(ExtraCodecs.COMPONENT).fieldOf("description").forGetter(i -> i.description)
        ).apply(builder, QuestLootNpcObjective::new);
    }).codec();

    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "objective.quest_loot_npc");
    protected ResourceLocation npcDefinition;
    protected double chanceToFind;
    private final int requiredCount;
    protected Component itemDescription;
    protected List<Component> description = new ArrayList<>();


    public QuestLootNpcObjective(String name, QuestStructureLocation structureLocation, ResourceLocation npcDefinition,
                                 double chance, int count, Component itemDescription,
                                 List<Component> description) {
        super(name, structureLocation);
        this.npcDefinition = npcDefinition;
        chanceToFind = chance;
        requiredCount = count;
        this.itemDescription = itemDescription;
        this.description.addAll(description);
    }

    @Override
    public QuestObjectiveType<? extends QuestObjective<?>> getType() {
        return QuestObjectiveTypes.QUEST_LOOT_NPC.get();
    }

    @Override
    public List<Component> getDescription() {
        return description;
    }

    private MutableComponent getDescriptionWithCount(int count) {
        NpcDefinition def = NpcDefinitionManager.getDefinition(npcDefinition);
        return Component.translatable("mknpc.objective.quest_loot_npc.desc", itemDescription, def.getDisplayName(),
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
            boolean applies = event.getEntity().getCapability(NpcCapabilities.ENTITY_NPC_DATA_CAPABILITY).map(
                    x -> x.getStructureId().map(structId -> structId.equals(objData.getUuid())).orElse(false)).orElse(false)
                    && def.getDefinitionName().equals(npcDefinition);
            if (applies && player.getRandom().nextDouble() <= chanceToFind) {
                int currentCount = objectiveData.getInt("lootCount");
                currentCount++;
                objectiveData.putInt("lootCount", currentCount);
                objectiveData.setDescription(getDescriptionWithCount(currentCount));
                player.sendSystemMessage(getProgressMessage(event.getEntity(), currentCount)
                        .withStyle(ChatFormatting.GOLD));
                if (currentCount == requiredCount) {
                    signalCompleted(objectiveData);
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
        return new UUIDInstanceData(entry.getStructureId());
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
        PlayerQuestObjectiveData newObj = new PlayerQuestObjectiveData(getObjectiveName(), getDescription());
        newObj.putInt("lootCount", 0);
        return newObj;
    }
}
