//package com.chaosbuffalo.mkultra.data_generators;
//
//
//import com.chaosbuffalo.mkultra.MKUltra;
//import com.chaosbuffalo.mkultra.init.MKUItems;
//import net.minecraft.data.DataGenerator;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.item.Item;
//import net.minecraftforge.client.model.generators.ItemModelBuilder;
//import net.minecraftforge.client.model.generators.ItemModelProvider;
//import net.minecraftforge.common.data.ExistingFileHelper;
//import net.minecraftforge.registries.ForgeRegistries;
//
//
//public class MKUItemModelProvider extends ItemModelProvider {
//
//
//    public MKUItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
//        super(generator, MKUltra.MODID, existingFileHelper);
//    }
//
//    @Override
//    protected void registerModels() {
//        makeSimpleItem(MKUItems.corruptedPigIronPlate.get());
//        makeSimpleItem(MKUItems.greenKnightHelmet.get());
//        makeSimpleItem(MKUItems.greenKnightChestplate.get());
//        makeSimpleItem(MKUItems.greenKnightBoots.get());
//        makeSimpleItem(MKUItems.greenKnightLeggings.get());
//        makeSimpleItem(MKUItems.trooperKnightBoots.get());
//        makeSimpleItem(MKUItems.trooperKnightChestplate.get());
//        makeSimpleItem(MKUItems.trooperKnightHelmet.get());
//        makeSimpleItem(MKUItems.trooperKnightLeggings.get());
//        makeSimpleItem(MKUItems.destroyedTrooperBoots.get());
//        makeSimpleItem(MKUItems.destroyedTrooperChestplate.get());
//        makeSimpleItem(MKUItems.destroyedTrooperLeggings.get());
//        makeSimpleItem(MKUItems.destroyedTrooperHelmet.get());
//    }
//
//    private void makeSimpleItem(Item item) {
//        String path = ForgeRegistries.ITEMS.getKey(item).getPath();
//
//        ItemModelBuilder builder = getBuilder(path)
//                .parent(getExistingFile(new ResourceLocation("item/generated")))
//                .texture("layer0", modLoc(String.format("items/%s", path)));
//    }
//}
