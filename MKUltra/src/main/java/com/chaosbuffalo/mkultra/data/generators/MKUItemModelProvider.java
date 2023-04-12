package com.chaosbuffalo.mkultra.data.generators;


import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.init.MKUItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;


public class MKUItemModelProvider extends ItemModelProvider {

    public MKUItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MKUltra.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        makeSimpleItem(MKUItems.corruptedPigIronPlate.get());
        makeSimpleItem(MKUItems.greenKnightHelmet.get());
        makeSimpleItem(MKUItems.greenKnightChestplate.get());
        makeSimpleItem(MKUItems.greenKnightBoots.get());
        makeSimpleItem(MKUItems.greenKnightLeggings.get());
        makeSimpleItem(MKUItems.trooperKnightBoots.get());
        makeSimpleItem(MKUItems.trooperKnightChestplate.get());
        makeSimpleItem(MKUItems.trooperKnightHelmet.get());
        makeSimpleItem(MKUItems.trooperKnightLeggings.get());
        makeSimpleItem(MKUItems.destroyedTrooperBoots.get());
        makeSimpleItem(MKUItems.destroyedTrooperChestplate.get());
        makeSimpleItem(MKUItems.destroyedTrooperLeggings.get());
        makeSimpleItem(MKUItems.destroyedTrooperHelmet.get());
    }

    private void makeSimpleItem(Item item) {
        basicItem(item);
    }
}