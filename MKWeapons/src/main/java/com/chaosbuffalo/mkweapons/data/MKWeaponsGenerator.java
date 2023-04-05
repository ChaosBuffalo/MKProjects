//package com.chaosbuffalo.mkweapons.data;
//
//import com.chaosbuffalo.mkweapons.MKWeapons;
//import net.minecraft.data.DataGenerator;
//import net.minecraftforge.common.data.BlockTagsProvider;
//import net.minecraftforge.common.data.ExistingFileHelper;
//import net.minecraftforge.data.event.GatherDataEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.common.Mod;
//
//@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
//public class MKWeaponsGenerator {
//    @SubscribeEvent
//    public static void gatherData(GatherDataEvent event) {
//        DataGenerator gen = event.getGenerator();
//        ExistingFileHelper helper = event.getExistingFileHelper();
//        if (event.includeServer()) {
//            // recipes here
//            gen.addProvider(true, new MKWeaponRecipeProvider(gen.getPackOutput()));
//            gen.addProvider(true, new MKWeaponTypesProvider(gen.getPackOutput()));
//            gen.addProvider(true, new LootTierProvider(gen.getPackOutput()));
//            BlockTagsProvider blockTagProvider = new BlockTagsProvider(gen.getPackOutput(), MKWeapons.MODID, helper) {
//                @Override
//                protected void addTags() {
//
//                }
//            };
//            gen.addProvider(true, blockTagProvider);
//            gen.addProvider(true, new MKWeaponsItemTagProvider(gen.getPackOutput(), blockTagProvider, MKWeapons.MODID, helper));
//        }
//        if (event.includeClient()) {
//            gen.addProvider(true, new MKWeaponModelProvider(gen.getPackOutput(), helper));
//        }
//    }
//}