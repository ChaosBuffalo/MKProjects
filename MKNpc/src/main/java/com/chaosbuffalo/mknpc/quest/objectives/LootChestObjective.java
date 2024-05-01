package com.chaosbuffalo.mknpc.quest.objectives;

import com.chaosbuffalo.mkcore.utils.CommonCodecs;
import com.chaosbuffalo.mknpc.capabilities.IChestNpcData;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.npc.NotableChestEntry;
import com.chaosbuffalo.mknpc.quest.QuestStructureLocation;
import com.chaosbuffalo.mknpc.quest.data.QuestData;
import com.chaosbuffalo.mknpc.quest.data.objective.UUIDInstanceData;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestObjectiveData;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LootChestObjective extends QuestObjective<UUIDInstanceData> implements IContainerObjectiveHandler {
    public static final Codec<LootChestObjective> CODEC = RecordCodecBuilder.<LootChestObjective>mapCodec(builder -> {
        return builder.group(
                Codec.STRING.fieldOf("objectiveName").forGetter(i -> i.objectiveName),
                QuestStructureLocation.CODEC.fieldOf("structure").forGetter(i -> i.location),
                Codec.STRING.fieldOf("chestTag").forGetter(i -> i.chestTag),
                CommonCodecs.ITEM_STACK.listOf().fieldOf("items").forGetter(i -> i.itemsToAdd),
                ExtraCodecs.COMPONENT.listOf().fieldOf("description").forGetter(i -> i.description)
        ).apply(builder, LootChestObjective::new);
    }).codec();

    private final List<ItemStack> itemsToAdd;
    private final String chestTag;
    protected List<Component> description;

    public LootChestObjective(String name, QuestStructureLocation structureLocation, String chestTag, List<ItemStack> items, List<Component> description) {
        super(name, structureLocation);
        this.chestTag = chestTag;
        itemsToAdd = ImmutableList.copyOf(items);
        this.description = ImmutableList.copyOf(description);
    }

    @Override
    public QuestObjectiveType<? extends QuestObjective<?>> getType() {
        return QuestObjectiveTypes.LOOT_CHEST.get();
    }

    @Override
    public List<Component> getDescription() {
        return description;
    }

    @Override
    public boolean isStructureRelevant(MKStructureEntry entry) {
        return location.getStructureId().equals(entry.getStructureName()) && entry.hasChestWithTag(chestTag);
    }

    @Override
    public UUIDInstanceData generateInstanceData(Map<ResourceLocation, List<MKStructureEntry>> questStructures) {
        MKStructureEntry entry = questStructures.get(location.getStructureId()).get(location.getIndex());
        Optional<NotableChestEntry> chest = entry.getFirstChestWithTag(chestTag);
        return chest.map(x -> new UUIDInstanceData(x.getChestId())).orElse(new UUIDInstanceData());
    }

    @Override
    public UUIDInstanceData instanceDataFactory() {
        return new UUIDInstanceData();
    }

    @Override
    public PlayerQuestObjectiveData generatePlayerData(IWorldNpcData worldData, QuestData questData) {
        UUIDInstanceData objData = getInstanceData(questData);
        PlayerQuestObjectiveData newObj = new PlayerQuestObjectiveData(getObjectiveName(), getDescription());
        NotableChestEntry chest = worldData.getNotableChest(objData.getUUID());
        if (chest != null) {
            newObj.putBlockPos("chestPos", chest.getLocation());
        }
        newObj.putBool("hasLooted", false);
        return newObj;
    }


    @Override
    public boolean onLootChest(Player player, PlayerQuestObjectiveData objectiveData, QuestData questData, IChestNpcData chestData) {
        UUIDInstanceData objData = getInstanceData(questData);
        if (objectiveData.getBool("hasLooted")) {
            return false;
        }
        if (chestData.getChestId() != null && chestData.getChestId().equals(objData.getUUID())) {
            objectiveData.putBool("hasLooted", true);
            objectiveData.removeBlockPos("chestPos");
            signalCompleted(objectiveData);
            populateChest(player, chestData);
            return true;
        }
        return false;
    }

    protected void populateChest(Player player, IChestNpcData chestData) {
        int index = 0;
        SimpleContainer inventory = chestData.getQuestInventoryForPlayer(player);
        for (ItemStack item : itemsToAdd) {
            inventory.setItem(index, item.copy());
            index++;
        }
    }
}
