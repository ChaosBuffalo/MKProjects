package com.chaosbuffalo.mkweapons.items.randomization.slots;

import com.chaosbuffalo.mkweapons.MKWeapons;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class LootSlotManager {

    public static final ResourceLocation INVALID_LOOT_SLOT = new ResourceLocation(MKWeapons.MODID,
            "loot_slot.invalid");

    public static final LootSlot MAIN_HAND = new LootSlot(new ResourceLocation(MKWeapons.MODID, "main_hand"),
            EquipmentSlot.MAINHAND);
    public static final LootSlot OFF_HAND = new LootSlot(new ResourceLocation(MKWeapons.MODID, "off_hand"),
            EquipmentSlot.OFFHAND);
    public static final LootSlot CHEST = new LootSlot(new ResourceLocation(MKWeapons.MODID, "chest"),
            EquipmentSlot.CHEST);
    public static final LootSlot LEGS = new LootSlot(new ResourceLocation(MKWeapons.MODID, "legs"),
            EquipmentSlot.LEGS);
    public static final LootSlot HEAD = new LootSlot(new ResourceLocation(MKWeapons.MODID, "head"),
            EquipmentSlot.HEAD);
    public static final LootSlot FEET = new LootSlot(new ResourceLocation(MKWeapons.MODID, "feet"),
            EquipmentSlot.FEET);
    public static final LootSlot INVALID = new LootSlot(INVALID_LOOT_SLOT, (ent, item) -> {});
    public static final LootSlot ITEMS = new LootSlot(new ResourceLocation(MKWeapons.MODID, "items"),
            (ent, item) -> {});
    public static final LootSlot RINGS = new LootSlot(new ResourceLocation(MKWeapons.MODID, "rings"),
            (ent, item) -> {});
    public static final LootSlot EARRINGS = new LootSlot(new ResourceLocation(MKWeapons.MODID, "earrings"),
            (ent, item) -> {});

    public static final Map<ResourceLocation, LootSlot> SLOTS = new HashMap<>();

    public static void addLootSlot(LootSlot slot){
        SLOTS.put(slot.getName(), slot);
    }

    public static LootSlot getSlotFromName(ResourceLocation name){
        return SLOTS.get(name);
    }

    public static void setupLootSlots(){
        addLootSlot(MAIN_HAND);
        addLootSlot(OFF_HAND);
        addLootSlot(CHEST);
        addLootSlot(LEGS);
        addLootSlot(HEAD);
        addLootSlot(FEET);
        addLootSlot(INVALID);
        addLootSlot(ITEMS);
        addLootSlot(RINGS);
        addLootSlot(EARRINGS);
    }
}
