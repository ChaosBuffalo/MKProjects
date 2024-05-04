package com.chaosbuffalo.mknpc.quest.objectives;

import com.chaosbuffalo.mkcore.utils.CommonCodecs;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.capabilities.NpcCapabilities;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.npc.NotableNpcEntry;
import com.chaosbuffalo.mknpc.quest.QuestStructureLocation;
import com.chaosbuffalo.mknpc.quest.data.QuestData;
import com.chaosbuffalo.mknpc.quest.data.objective.UUIDInstanceData;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestChainInstance;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestObjectiveData;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.RecipeMatcher;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TradeItemsObjective extends QuestObjective<UUIDInstanceData> implements ITradeObjectiveHandler {
    public static final Codec<TradeItemsObjective> CODEC = RecordCodecBuilder.<TradeItemsObjective>mapCodec(builder -> {
        return builder.group(
                Codec.STRING.fieldOf("objectiveName").forGetter(i -> i.objectiveName),
                QuestStructureLocation.CODEC.fieldOf("structure").forGetter(i -> i.location),
                ResourceLocation.CODEC.fieldOf("npcDefinition").forGetter(i -> i.npcDefinition),
                CommonCodecs.ITEM_STACK.listOf().fieldOf("items").forGetter(i -> i.neededItems)
        ).apply(builder, TradeItemsObjective::new);
    }).codec();

    private final List<ItemStack> neededItems;
    private final ResourceLocation npcDefinition;

    public TradeItemsObjective(String name, QuestStructureLocation structureLocation, ResourceLocation npcDefinition, List<ItemStack> items) {
        super(name, structureLocation);
        this.npcDefinition = npcDefinition;
        neededItems = ImmutableList.copyOf(items);
    }

    @Override
    public QuestObjectiveType<? extends QuestObjective<?>> getType() {
        return QuestObjectiveTypes.TRADE_WITH_NPC.get();
    }

    @Override
    public List<Component> getDescription() {
        return neededItems.stream()
                .map(x -> Component.translatable("mknpc.trade.item_needed", x.getCount(), x.getHoverName()))
                .collect(Collectors.toList());
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
    public PlayerQuestObjectiveData generatePlayerData(IWorldNpcData worldData, QuestData questData) {
        UUIDInstanceData objData = getInstanceData(questData);
        PlayerQuestObjectiveData newObj = new PlayerQuestObjectiveData(getObjectiveName(), getDescription());
        NotableNpcEntry entry = worldData.getNotableNpc(objData.getUUID());
        if (entry != null) {
            newObj.putBlockPos("npcPos", entry.getLocation());
        }
        return newObj;
    }

    @Override
    public void onPlayerTradeSuccess(Player player, PlayerQuestObjectiveData objectiveData,
                                     QuestData questData, PlayerQuestChainInstance playerChain, LivingEntity trader) {
        player.sendSystemMessage(Component.translatable("mknpc.quest.trade.accepted",
                trader.getDisplayName()).withStyle(ChatFormatting.GOLD));
        signalCompleted(objectiveData);
    }

    @Override
    public boolean canTradeWith(LivingEntity trader, Player player, PlayerQuestObjectiveData objectiveData,
                                QuestData questData, PlayerQuestChainInstance chainInstance) {
        UUIDInstanceData objData = getInstanceData(questData);
        return trader.getCapability(NpcCapabilities.ENTITY_NPC_DATA_CAPABILITY)
                .map(x -> x.getNotableUUID().equals(objData.getUUID())).orElse(false);
    }

    @Nullable
    public int[] findMatches(List<ItemStack> nonEmptyInventoryContents) {
        return RecipeMatcher.findMatches(nonEmptyInventoryContents, neededItems.stream().map(
                TradeItemsObjective::getItemsEqualTester).collect(Collectors.toList()));
    }

    public static Predicate<ItemStack> getItemsEqualTester(ItemStack other) {
        return itemStack -> ItemStack.matches(itemStack, other);
    }
}
