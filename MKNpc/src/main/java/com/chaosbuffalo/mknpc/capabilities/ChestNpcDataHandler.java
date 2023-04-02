package com.chaosbuffalo.mknpc.capabilities;

import com.chaosbuffalo.mknpc.inventories.PsuedoChestContainer;
import com.chaosbuffalo.mknpc.inventories.QuestChestInventory;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChestNpcDataHandler implements IChestNpcData{
    @Nullable
    private UUID structureId;
    @Nullable
    private UUID chestId;
    private boolean needsUploadToWorld;
    private boolean placedByStructure;
    private final ChestBlockEntity entity;
    @Nullable
    private String chestLabel;
    @Nullable
    private ResourceLocation structureName;

    private final HashMap<UUID, QuestChestInventory> questInventories = new HashMap<>();

    public ChestNpcDataHandler(ChestBlockEntity entity) {
        this.entity = entity;
        structureId = null;
        chestId = null;
        needsUploadToWorld = false;
        placedByStructure = false;
        chestLabel = null;
        structureName = null;
    }

    public QuestChestInventory createQuestInventoryForPlayer(UUID playerId){
        QuestChestInventory inventory = new QuestChestInventory(getTileEntity());
        return inventory;
    }

    @Override
    public QuestChestInventory getQuestInventoryForPlayer(Player player){
        return questInventories.computeIfAbsent(player.getUUID(), this::createQuestInventoryForPlayer);
    }

    @Override
    public boolean hasQuestInventoryForPlayer(Player player){
        return questInventories.containsKey(player.getUUID());
    }


    @Nullable
    @Override
    public UUID getChestId() {
        return chestId;
    }

    @Nullable
    @Override
    public String getChestLabel() {
        return chestLabel;
    }

    @Override
    public boolean isInsideStructure() {
        return structureName != null && structureId != null;
    }

    @Nullable
    @Override
    public UUID getStructureId() {
        return structureId;
    }

    @Nullable
    @Override
    public ResourceLocation getStructureName() {
        return structureName;
    }

    @Override
    public void setStructureName(ResourceLocation name) {
        structureName = name;
    }

    @Override
    public void setStructureId(UUID id) {
        structureId = id;
    }

    @Override
    public GlobalPos getGlobalBlockPos() {
        return GlobalPos.of(entity.getLevel().dimension(), entity.getBlockPos());
    }

    @Nullable
    @Override
    public Level getStructureWorld() {
        return getTileEntity().getLevel();
    }

    @Override
    public ChestBlockEntity getTileEntity() {
        return entity;
    }

    @Override
    public void generateChestId(String chestLabel) {
        needsUploadToWorld = true;
        placedByStructure = true;
        chestId = UUID.randomUUID();
        this.chestLabel = chestLabel;
    }



    @Override
    public void onLoad() {
        if (needsUploadToWorld){
            Level world = getTileEntity().getLevel();
            if (world != null && !world.isClientSide()) {
                MinecraftServer server = world.getServer();
                if (server != null){
                    Level overworld = server.getLevel(Level.OVERWORLD);
                    if (overworld != null){
                        overworld.getCapability(NpcCapabilities.WORLD_NPC_DATA_CAPABILITY)
                                .ifPresent(cap -> cap.addChest(this));
                    }
                    needsUploadToWorld = false;
                }
            }
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("placedByStructure", placedByStructure);
        tag.putBoolean("needsUploadToWorld", needsUploadToWorld);
        if (chestId != null){
            tag.putUUID("chestId", chestId);
        }
        if (structureId != null){
            tag.putUUID("structureId", structureId);
        }
        if (chestLabel != null){
            tag.putString("chestLabel", chestLabel);
        }
        if (structureName != null){
            tag.putString("structureName", structureName.toString());
        }
        CompoundTag questInvNbt = new CompoundTag();
        for (Map.Entry<UUID, QuestChestInventory> entry : questInventories.entrySet()){
            questInvNbt.put(entry.getKey().toString(), entry.getValue().createTag());
        }
        tag.put("questInventories", questInvNbt);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        placedByStructure = nbt.getBoolean("placedByStructure");
        needsUploadToWorld = nbt.getBoolean("needsUploadToWorld");
        if (nbt.contains("chestId")){
            chestId = nbt.getUUID("chestId");
        }
        if (nbt.contains("structureId")){
            structureId = nbt.getUUID("structureId");
        }
        if (nbt.contains("chestLabel")){
            chestLabel = nbt.getString("chestLabel");
        }
        if (nbt.contains("structureName")){
            structureName = new ResourceLocation(nbt.getString("structureName"));
        }
        if (nbt.contains("questInventories")){
            questInventories.clear();
            CompoundTag questInvNbt = nbt.getCompound("questInventories");
            for (String key : questInvNbt.getAllKeys()){
                QuestChestInventory newInventory = new QuestChestInventory(entity);
                newInventory.fromTag(questInvNbt.getList(key, Tag.TAG_COMPOUND));
                questInventories.put(UUID.fromString(key), newInventory);
            }
        }

    }

    @Override
    public Component getDisplayName() {
        return new TextComponent("Quest Chest");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int guiWindow, Inventory playerInventory, Player player) {
        return PsuedoChestContainer.createGeneric9X3(guiWindow, playerInventory, getQuestInventoryForPlayer(player), entity);
    }
}
