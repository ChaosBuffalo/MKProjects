package com.chaosbuffalo.mkweapons.data;

import com.chaosbuffalo.mkweapons.MKWeapons;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class MKWeaponsGenerator {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper helper = event.getExistingFileHelper();
        if (event.includeServer()) {
            // recipes here
            gen.addProvider(new MKWeaponRecipeProvider(gen));
            gen.addProvider(new MKWeaponTypesProvider(gen));
            gen.addProvider(new LootTierProvider(gen));
            BlockTagsProvider blockTagProvider = new BlockTagsProvider(gen, MKWeapons.MODID, helper) {
                @Override
                protected void addTags() {

                }
            };
            gen.addProvider(blockTagProvider);
            gen.addProvider(new MKWeaponsItemTagProvider(gen, blockTagProvider, MKWeapons.MODID, helper));
        }
        if (event.includeClient()) {
            gen.addProvider(new MKWeaponModelProvider(gen, helper));
        }
    }
}