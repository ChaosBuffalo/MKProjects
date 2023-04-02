package com.chaosbuffalo.mknpc.quest.objectives;

import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkcore.utils.SerializationUtils;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.capabilities.NpcCapabilities;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.npc.NotableNpcEntry;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.common.util.RecipeMatcher;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TradeItemsObjective extends StructureInstanceObjective<UUIDInstanceData> implements ITradeObjectiveHandler{
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "objective.trade_with_npc");
    protected ResourceLocationAttribute npcDefinition = new ResourceLocationAttribute("npcDefinition", NpcDefinitionManager.INVALID_NPC_DEF);
    private final List<ItemStack> neededItems = new ArrayList<>();

    public TradeItemsObjective(String name, ResourceLocation structure, int index, ResourceLocation npcDefinition, MutableComponent... description){
        super(NAME, name, structure, index, description);
        addAttribute(this.npcDefinition);
        this.npcDefinition.setValue(npcDefinition);
    }

    public TradeItemsObjective(){
        super(NAME, "invalid", defaultDescription);
        addAttribute(this.npcDefinition);
    }

    public void addItemStack(ItemStack stack){
        neededItems.add(stack);
        setDescription(neededItems.stream().map(x -> new TranslatableComponent("mknpc.trade.item_needed",
                x.getCount(), x.getHoverName())).collect(Collectors.toList()));
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
    public PlayerQuestObjectiveData generatePlayerData(IWorldNpcData worldData, QuestData questData) {
        UUIDInstanceData objData = getInstanceData(questData);
        PlayerQuestObjectiveData newObj = playerDataFactory();
        NotableNpcEntry entry = worldData.getNotableNpc(objData.getUuid());
        if (entry != null) {
            newObj.putBlockPos("npcPos", entry.getLocation());
        }
        return newObj;
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        List<ItemStack> dStacks = dynamic.get("items").asList(SerializationUtils::deserializeItemStack);
        neededItems.clear();
        neededItems.addAll(dStacks);
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("items"),
                ops.createList(neededItems.stream().map(x -> SerializationUtils.serializeItemStack(ops, x)))
        );
    }

    @Override
    public void onPlayerTradeSuccess(Player player, PlayerQuestObjectiveData objectiveData,
                                     QuestData questData, PlayerQuestChainInstance playerChain, LivingEntity trader) {
        player.sendMessage(new TranslatableComponent("mknpc.quest.trade.accepted",
                trader.getDisplayName()).withStyle(ChatFormatting.GOLD), Util.NIL_UUID);
        signalCompleted(objectiveData);
    }

    @Override
    public boolean canTradeWith(LivingEntity trader, Player player, PlayerQuestObjectiveData objectiveData,
                                QuestData questData, PlayerQuestChainInstance chainInstance) {
        UUIDInstanceData objData = getInstanceData(questData);
        return trader.getCapability(NpcCapabilities.ENTITY_NPC_DATA_CAPABILITY)
                .map(x -> x.getNotableUUID().equals(objData.getUuid())).orElse(false);
    }

    @Nullable
    public int[] findMatches(List<ItemStack> nonEmptyInventoryContents){
        return RecipeMatcher.findMatches(nonEmptyInventoryContents, neededItems.stream().map(
                TradeItemsObjective::getItemsEqualTester).collect(Collectors.toList()));
    }

    @Override
    public PlayerQuestObjectiveData playerDataFactory() {
        return new PlayerQuestObjectiveData(getObjectiveName(), getDescription());
    }

    public static Predicate<ItemStack> getItemsEqualTester(ItemStack other){
        return itemStack -> ItemStack.matches(itemStack, other);
    }
}
