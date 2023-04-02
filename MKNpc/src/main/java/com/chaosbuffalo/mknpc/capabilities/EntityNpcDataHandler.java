package com.chaosbuffalo.mknpc.capabilities;

import com.chaosbuffalo.mkchat.capabilities.ChatCapabilities;
import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.INotifyOnEntityDeath;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcDefinitionManager;
import com.chaosbuffalo.mknpc.npc.entries.LootOptionEntry;
import com.chaosbuffalo.mknpc.npc.entries.QuestOfferingEntry;
import com.chaosbuffalo.mknpc.quest.QuestChainInstance;
import com.chaosbuffalo.mknpc.quest.QuestDefinition;
import com.chaosbuffalo.mknpc.quest.QuestDefinitionManager;
import com.chaosbuffalo.mknpc.utils.RandomCollection;
import com.chaosbuffalo.mkweapons.items.randomization.LootConstructor;
import com.chaosbuffalo.mkweapons.items.randomization.LootTier;
import com.chaosbuffalo.mkweapons.items.randomization.LootTierManager;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlotManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class EntityNpcDataHandler implements IEntityNpcData {
    private final LivingEntity entity;
    private NpcDefinition definition;
    private boolean mkSpawned;
    private int bonusXp;
    private UUID spawnID;
    private BlockPos blockPos;
    private boolean notable;
    private boolean needsDefinitionApplied;
    private double noLootChance;
    private int dropChances;
    private double noLootChanceIncrease;
    private boolean shouldHaveQuest;
    private double difficultyValue;
    private final List<LootOptionEntry> options;
    private final Map<ResourceLocation, UUID> questOfferings = new HashMap<>();
    private final Queue<QuestOfferingEntry> questRequests = new ArrayDeque<>();
    private int questGenCd;
    private UUID notableId;
    @Nullable
    private UUID structureId;
    @Nullable
    private INotifyOnEntityDeath deathReceiver;

    public EntityNpcDataHandler(LivingEntity entity) {
        this.entity = entity;
        deathReceiver = null;
        mkSpawned = false;
        bonusXp = 0;
        notable = false;
        spawnID = UUID.randomUUID();
        notableId = Util.NIL_UUID;
        structureId = null;
        needsDefinitionApplied = false;
        noLootChance = 0;
        dropChances = 0;
        noLootChanceIncrease = 0;
        shouldHaveQuest = false;
        options = new ArrayList<>();
        questGenCd = 0;
        difficultyValue = 0.0;
    }

    public boolean needsDefinitionApplied() {
        return needsDefinitionApplied;
    }

    public void applyDefinition(){
        if (definition != null){
            definition.applyDefinition(getEntity(), difficultyValue);
            needsDefinitionApplied = false;
        }
    }

    @Override
    public void addLootOption(LootOptionEntry option) {
        options.add(option);
    }


    @Override
    public void setChanceNoLoot(double chance) {
        noLootChance = chance;
    }

    @Override
    public void setDropChances(int count) {
        dropChances = count;
    }

    @Override
    public void setNoLootChanceIncrease(double chance) {
        noLootChanceIncrease = chance;
    }

    public double getNoLootChance() {
        return noLootChance;
    }

    @Override
    public void handleExtraLoot(int lootingLevel, Collection<ItemEntity> drops, DamageSource source) {
        LivingEntity entity = getEntity();
        double noLoot = getNoLootChance();
        for (int i = 0; i < dropChances + lootingLevel; i++){
            if (entity.getRandom().nextDouble() >= noLoot){
                RandomCollection<LootOptionEntry> rolls = new RandomCollection<>();
                for (LootOptionEntry option : options){
                    if (option.isValidConfiguration()){
                        rolls.add(option.weight, option);
                    }
                }
                if (rolls.size() > 0){
                    LootOptionEntry selected = rolls.next();
                    LootSlot lootSlot = LootSlotManager.getSlotFromName(selected.lootSlotName);
                    LootTier lootTier = LootTierManager.getTierFromName(selected.lootTierName);
                    if (lootSlot != null && lootTier != null){
                        LootConstructor constructor = lootTier.generateConstructorForSlot(entity.getRandom(), lootSlot);
                        if (constructor != null){
                            ItemStack item = constructor.constructItem(entity.getRandom(), getDifficultyValue());
                            if (!item.isEmpty()){
                                drops.add(entity.spawnAtLocation(item));
                            }
                        }
                    }
                }
            }
            noLoot += noLootChanceIncrease;
        }
    }

    private void handleQuestRequests(){
        QuestOfferingEntry entry = questRequests.poll();
        if (entry == null){
            return;
        }
        MinecraftServer server = getEntity().getServer();
        QuestDefinition npcDef = QuestDefinitionManager.getDefinition(entry.getQuestDef());
        if (npcDef == null){
            MKNpc.LOGGER.debug("Can't find definition for quest {}", entry.getQuestDef());
            questRequests.add(entry);
            return;
        }
        if (server != null && entry.getQuestId() == null){
            Level overworld = server.getLevel(Level.OVERWORLD);
            if (overworld != null){
                Optional<QuestChainInstance.QuestChainBuildResult> quest = overworld.getCapability(NpcCapabilities.WORLD_NPC_DATA_CAPABILITY)
                        .map(x -> x.buildQuest(npcDef, getSpawnPos())).orElse(Optional.empty());
                if (quest.isPresent()) {
                    QuestChainInstance.QuestChainBuildResult result = quest.get();
                    QuestChainInstance newQuest = result.instance;
                    MKNpc.getNpcData(entity).ifPresent(x -> newQuest.setQuestSourceNpc(x.getNotableUUID()));
                    MKNpc.LOGGER.debug("Assigning quest {}({}) to {}", newQuest.getDefinition().getName(), newQuest.getQuestId(), entity);
                    entry.setupDialogue(result);
                    entry.setQuestId(newQuest.getQuestId());
                }
            }
        }
        if (entry.getQuestId() != null){
            MKNpc.LOGGER.debug("Adding offering for start quest {} to {}", entry.getQuestDef(), entity);
            addQuestOffering(entry.getQuestDef(), entry.getQuestId());
            if (entry.getTree() == null){
                MKNpc.LOGGER.error("{} has quest offering for {} but no dialogue tree, dialogue won't be assigned. " +
                        "There is probably a bug in this quest.", entity, entry.getQuestDef());
            }
            if (entry.getTree() != null){
                MKNpc.LOGGER.debug("Adding dialogue offering for start quest {} to {}", entry.getQuestDef(), entity);
                entity.getCapability(ChatCapabilities.NPC_DIALOGUE_CAPABILITY).ifPresent(
                        chat -> chat.addAdditionalDialogueTree(entry.getTree()));
            }
        } else {
            MKNpc.LOGGER.debug("Failed to generate quest request for: {} entity is {}", entry.getQuestDef(), entity);
            questRequests.add(entry);
        }
    }

    @Override
    public void tick(){
        if (questGenCd <= 0){
            if (questRequests.size() > 0){
                handleQuestRequests();
                questGenCd = entity.getRandom().nextInt(GameConstants.TICKS_PER_SECOND * 5);
            }
        } else {
            questGenCd--;
        }
    }

    @Override
    public void requestQuest(QuestOfferingEntry entry) {
        MKNpc.LOGGER.debug("Adding quest request for {} to {}", entry.getQuestDef(), entity);
        questRequests.add(entry);
    }

    @Override
    public Optional<INotifyOnEntityDeath> getDeathReceiver() {
        return Optional.ofNullable(deathReceiver);
    }

    @Override
    public void setDeathReceiver(INotifyOnEntityDeath receiver) {
        deathReceiver = receiver;
    }

    @Override
    public LivingEntity getEntity() {
        return entity;
    }

    @Override
    public NpcDefinition getDefinition() {
        return definition;
    }

    @Override
    public double getDifficultyValue() {
        return difficultyValue;
    }

    @Override
    public void setDifficultyValue(double difficultyValue) {
        this.difficultyValue = difficultyValue;
    }

    @Override
    public void setDefinition(NpcDefinition definition) {
        this.definition = definition;
    }

    @Override
    public int getBonusXp() {
        return bonusXp;
    }

    @Override
    public void setBonusXp(int value) {
        this.bonusXp = value;
    }

    @Override
    public boolean wasMKSpawned() {
        return mkSpawned;
    }

    @Override
    public void setSpawnPos(BlockPos pos) {
        this.blockPos = pos;
    }

    @Override
    public void addQuestOffering(ResourceLocation questName, UUID questId) {
        questOfferings.put(questName, questId);
    }

    @Override
    public boolean hasGeneratedQuest() {
        return !questOfferings.isEmpty();
    }

    @Override
    public boolean shouldHaveQuest() {
        return shouldHaveQuest;
    }

    @Override
    public void putShouldHaveQuest(boolean value) {
        shouldHaveQuest = value;
    }

    @Override
    public Map<ResourceLocation, UUID> getQuestsOffered() {
        return questOfferings;
    }

    @Override
    public BlockPos getSpawnPos() {
        return blockPos;
    }

    @Override
    public void setSpawnID(UUID id) {
        spawnID = id;
    }

    @Nonnull
    @Override
    public UUID getSpawnID() {
        return spawnID;
    }

    @Override
    public void setStructureId(UUID structureId) {
        this.structureId = structureId;
    }

    @Override
    public Optional<UUID> getStructureId() {
        return Optional.ofNullable(structureId);
    }

    @Override
    public void setMKSpawned(boolean value) {
        this.mkSpawned = value;
    }

    @Override
    public boolean isNotable() {
        return notable;
    }

    @Override
    public void setNotable(boolean value) {
        notable = value;
    }

    @Override
    public UUID getNotableUUID() {
        return notableId;
    }

    @Override
    public void setNotableUUID(UUID notableUUID) {
        this.notableId = notableUUID;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if (definition != null){
            tag.putString("npc_definition", definition.getDefinitionName().toString());
        }
        tag.putUUID("spawn_id", spawnID);
        tag.putBoolean("mk_spawned", mkSpawned);
        tag.putDouble("difficulty_value", difficultyValue);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("mk_spawned")){
            mkSpawned = nbt.getBoolean("mk_spawned");
        }
        if (nbt.contains("spawn_id")){
            spawnID = nbt.getUUID("spawn_id");
        }
        if (nbt.contains("npc_definition")){
            ResourceLocation defName = new ResourceLocation(nbt.getString("npc_definition"));
            this.definition = NpcDefinitionManager.getDefinition(defName);
            needsDefinitionApplied = true;
        }
        if (nbt.contains("difficulty_value")) {
            difficultyValue = nbt.getDouble("difficulty_value");
        }
    }
}
