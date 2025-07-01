package com.chaosbuffalo.mkweapons.items.randomization.slots;

import com.chaosbuffalo.mkweapons.MKWeapons;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;

import java.util.HashMap;
import java.util.Map;

public class LootSlotManager {

    public static final LootSlot MAIN_HAND = new LootSlot(MKWeapons.id("main_hand"), EquipmentSlotGroup.MAINHAND);
    public static final LootSlot OFF_HAND = new LootSlot(MKWeapons.id("off_hand"), EquipmentSlotGroup.OFFHAND);
    public static final LootSlot CHEST = new LootSlot(MKWeapons.id("chest"), EquipmentSlotGroup.CHEST);
    public static final LootSlot LEGS = new LootSlot(MKWeapons.id("legs"), EquipmentSlotGroup.LEGS);
    public static final LootSlot HEAD = new LootSlot(MKWeapons.id("head"), EquipmentSlotGroup.HEAD);
    public static final LootSlot FEET = new LootSlot(MKWeapons.id("feet"), EquipmentSlotGroup.FEET);

    public static final LootSlot ITEMS = new LootSlot(MKWeapons.id("items"), EquipmentSlotGroup.ANY);
    public static final LootSlot RINGS = new LootSlot(MKWeapons.id("rings"), EquipmentSlotGroup.ARMOR);
    public static final LootSlot EARRINGS = new LootSlot(MKWeapons.id("earrings"), EquipmentSlotGroup.ARMOR);

    public static final LootSlot HANDS = new LootSlot(MKWeapons.id("hands"), EquipmentSlotGroup.ARMOR);

    public static final Map<ResourceLocation, LootSlot> SLOTS = new HashMap<>();

    public static void addLootSlot(LootSlot slot) {
        SLOTS.put(slot.getName(), slot);
    }

    public static LootSlot getSlotFromName(ResourceLocation name) {
        return SLOTS.get(name);
    }

    static {
        addLootSlot(MAIN_HAND);
        addLootSlot(OFF_HAND);
        addLootSlot(CHEST);
        addLootSlot(LEGS);
        addLootSlot(HEAD);
        addLootSlot(FEET);
        addLootSlot(ITEMS);
        addLootSlot(RINGS);
        addLootSlot(EARRINGS);
        addLootSlot(HANDS);
    }
}
