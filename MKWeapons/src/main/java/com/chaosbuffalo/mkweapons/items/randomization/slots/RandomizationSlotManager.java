package com.chaosbuffalo.mkweapons.items.randomization.slots;

import com.chaosbuffalo.mkweapons.MKWeapons;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;

import java.util.HashMap;
import java.util.Map;

public class RandomizationSlotManager {

    public static final ResourceLocation INVALID_SLOT = new ResourceLocation(MKWeapons.MODID,
            "randomization.invalid");

    public static final IRandomizationSlot ATTRIBUTE_SLOT = new RandomizationSlot(
            new ResourceLocation(MKWeapons.MODID, "randomization.attributes"), ChatFormatting.DARK_GREEN, false);

    public static final IRandomizationSlot EFFECT_SLOT = new RandomizationSlot(
            new ResourceLocation(MKWeapons.MODID, "randomization.effect"), ChatFormatting.AQUA, false);

    public static final IRandomizationSlot ABILITY_SLOT = new RandomizationSlot(
            new ResourceLocation(MKWeapons.MODID, "randomization.ability"), ChatFormatting.AQUA, false);

    public static final IRandomizationSlot NAME_SLOT = new RandomizationSlot(
            new ResourceLocation(MKWeapons.MODID, "randomization.name"), ChatFormatting.WHITE, true);

    public static final IRandomizationSlot PERM_ATTRIBUTE_SLOT = new RandomizationSlot(
            new ResourceLocation(MKWeapons.MODID, "randomization.attributes_perm"), ChatFormatting.DARK_GREEN, true);

    public static final IRandomizationSlot PERM_EFFECT_SLOT = new RandomizationSlot(
            new ResourceLocation(MKWeapons.MODID, "randomization.effect_perm"), ChatFormatting.AQUA, true);

    public static final IRandomizationSlot PERM_ABILITY_SLOT = new RandomizationSlot(
            new ResourceLocation(MKWeapons.MODID, "randomization.ability_perm"), ChatFormatting.AQUA, true);


    public static final Map<ResourceLocation, IRandomizationSlot> SLOTS = new HashMap<>();

    public static void addRandomizatiotSlot(IRandomizationSlot slot){
        SLOTS.put(slot.getName(), slot);
    }

    public static IRandomizationSlot getSlotFromName(ResourceLocation name){
        return SLOTS.get(name);
    }

    public static void setupRandomizationSlots(){
        addRandomizatiotSlot(ATTRIBUTE_SLOT);
        addRandomizatiotSlot(EFFECT_SLOT);
        addRandomizatiotSlot(ABILITY_SLOT);
        addRandomizatiotSlot(NAME_SLOT);
        addRandomizatiotSlot(PERM_ABILITY_SLOT);
        addRandomizatiotSlot(PERM_EFFECT_SLOT);
        addRandomizatiotSlot(PERM_ATTRIBUTE_SLOT);
    }
}
