package com.chaosbuffalo.mknpc.quest.objectives;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.serialization.attributes.DoubleAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.capabilities.NpcCapabilities;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.npc.NotableNpcEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcDefinitionManager;
import com.chaosbuffalo.mknpc.quest.data.QuestData;
import com.chaosbuffalo.mknpc.quest.data.objective.UUIDInstanceData;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestChainInstance;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestObjectiveData;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class QuestLootNotableObjective extends StructureInstanceObjective<UUIDInstanceData> implements IKillObjectiveHandler {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "objective.quest_loot_notable");
    protected ResourceLocationAttribute npcDefinition = new ResourceLocationAttribute("npcDefinition", NpcDefinitionManager.INVALID_NPC_DEF);
    protected DoubleAttribute chanceToFind = new DoubleAttribute("chance", 1.0);
    protected IntAttribute count = new IntAttribute("count", 1);
    protected MutableComponent itemDescription;
    protected static final MutableComponent defaultItemDescription = new TextComponent("Placeholder Item");


    public QuestLootNotableObjective(String name, ResourceLocation structure, int index, ResourceLocation npcDefinition,
                                 double chance, int count, MutableComponent itemDescription,
                                 MutableComponent... description) {
        super(NAME, name, structure, index, description);
        this.npcDefinition.setValue(npcDefinition);
        this.chanceToFind.setValue(chance);
        this.count.setValue(count);
        this.itemDescription = itemDescription;
        addAttributes(this.npcDefinition, this.chanceToFind, this.count);
    }

    public QuestLootNotableObjective() {
        super(NAME, "invalid", defaultDescription);
        itemDescription = defaultItemDescription;
        addAttributes(npcDefinition, chanceToFind, count);
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("itemDescription"), ops.createString(Component.Serializer.toJson(itemDescription)));
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        itemDescription = Component.Serializer.fromJson(dynamic.get("itemDescription").asString()
                .resultOrPartial(MKNpc.LOGGER::error).orElseThrow(IllegalArgumentException::new));
    }

    private MutableComponent getDescriptionWithCount(Component name, int count) {
        return new TranslatableComponent("mknpc.objective.quest_loot_npc.desc", itemDescription, name,
                MKAbility.INTEGER_FORMATTER.format(count), MKAbility.INTEGER_FORMATTER.format(this.count.value()));
    }

    private MutableComponent getProgressMessage(LivingEntity entity, int count) {
        return new TranslatableComponent("mknpc.objective.quest_loot_npc.progress", itemDescription, entity.getName(),
                MKAbility.INTEGER_FORMATTER.format(count), MKAbility.INTEGER_FORMATTER.format(this.count.value()));
    }

    @Override
    public boolean onPlayerKillNpcDefEntity(Player player, PlayerQuestObjectiveData objectiveData, NpcDefinition def,
                                         LivingDeathEvent event, QuestData quest, PlayerQuestChainInstance playerChain) {
        if (!isComplete(objectiveData)) {
            UUIDInstanceData objData = getInstanceData(quest);
            boolean applies = event.getEntityLiving().getCapability(NpcCapabilities.ENTITY_NPC_DATA_CAPABILITY).map(
                    x -> x.getNotableUUID().equals(objData.getUuid())).orElse(false);
            if (applies && player.getRandom().nextDouble() <= chanceToFind.value()) {
                int currentCount = objectiveData.getInt("lootCount");
                currentCount++;
                objectiveData.putInt("lootCount", currentCount);
                objectiveData.setDescription(getDescriptionWithCount(event.getEntityLiving().getName(), currentCount));
                player.sendMessage(getProgressMessage(event.getEntityLiving(), currentCount)
                        .withStyle(ChatFormatting.GOLD), Util.NIL_UUID);
                if (currentCount == count.value()) {
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
        MKStructureEntry entry = questStructures.get(getStructureName()).get(structureIndex.value());
        Optional<NotableNpcEntry> npcOpt = entry.getFirstNotableOfType(npcDefinition.getValue());
        return npcOpt.map(x -> new UUIDInstanceData(x.getNotableId())).orElse(new UUIDInstanceData());
    }

    @Override
    public UUIDInstanceData instanceDataFactory() {
        return new UUIDInstanceData();
    }


    @Override
    public PlayerQuestObjectiveData playerDataFactory() {
        return new PlayerQuestObjectiveData(getObjectiveName(), getDescription());
    }

    @Override
    public boolean isStructureRelevant(MKStructureEntry entry) {
        return structureName.getValue().equals(entry.getStructureName()) && entry.hasNotableOfType(npcDefinition.getValue());
    }

    @Override
    public PlayerQuestObjectiveData generatePlayerData(IWorldNpcData worldData, QuestData questData) {
        UUIDInstanceData objData = getInstanceData(questData);
        PlayerQuestObjectiveData newObj = playerDataFactory();
        NotableNpcEntry notable = worldData.getNotableNpc(objData.getUuid());
        if (notable != null) {
            newObj.setDescription(getDescriptionWithCount(notable.getName(), 0));
            newObj.putBlockPos("npcPos", notable.getLocation());
        }
        newObj.putInt("lootCount", 0);
        return newObj;
    }
}