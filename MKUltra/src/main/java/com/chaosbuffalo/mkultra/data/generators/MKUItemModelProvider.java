package com.chaosbuffalo.mkultra.data.generators;


import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.init.MKUItems;
import com.chaosbuffalo.mkweapons.data.MKWeaponModelProvider;
import com.chaosbuffalo.mkweapons.items.MKBow;
import com.chaosbuffalo.mkweapons.items.MKMeleeWeapon;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.Objects;


public class MKUItemModelProvider extends MKWeaponModelProvider {

    public MKUItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, existingFileHelper, MKUltra.MODID);
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
        makeSimpleItem(MKUItems.seawovenScrap.get());
        makeSimpleItem(MKUItems.seawovenBoots.get());
        makeSimpleItem(MKUItems.seawovenChestplate.get());
        makeSimpleItem(MKUItems.seawovenLeggings.get());
        makeSimpleItem(MKUItems.seawovenHelmet.get());
        makeSimpleItem(MKUItems.ancientBronzeChestplate.get());
        makeSimpleItem(MKUItems.ancientBronzeHelmet.get());
        makeSimpleItem(MKUItems.ancientBronzeLeggings.get());
        makeSimpleItem(MKUItems.ancientBronzeBoots.get());
        makeSimpleItem(MKUItems.corruptedGauntlets.get());
        makeSimpleItem(MKUItems.necrotideBand.get());
        makeSimpleItem(MKUItems.ancientPriestBoots.get());
        makeSimpleItem(MKUItems.ancientPriestChestplate.get());
        makeSimpleItem(MKUItems.ancientPriestLeggings.get());
        makeSimpleItem(MKUItems.ancientPriestHelmet.get());
        makeSimpleItem(MKUItems.themnianBoots.get());
        makeSimpleItem(MKUItems.themnianChestplate.get());
        makeSimpleItem(MKUItems.themnianLeggings.get());
        makeSimpleItem(MKUItems.themnianHelmet.get());
        for (MKMeleeWeapon weapon : MKUItems.WEAPONS) {
            makeWeaponModel(weapon);
        }
        for (MKBow bow : MKUItems.BOWS) {
            makeBowModels(bow);
        }
        projectileItem(MKUItems.holyWordProjectileItem.get());
        makeSimpleItem(MKUItems.themnianLeaderBoots.get());
        makeSimpleItem(MKUItems.themnianLeaderChestplate.get());
        makeSimpleItem(MKUItems.themnianLeaderLeggings.get());
        makeSimpleItem(MKUItems.themnianLeaderHelmet.get());
    }

    private void makeSimpleItem(Item item) {
        basicItem(item);
    }

    public ItemModelBuilder projectileItem(Item item) {
        return projectileItem(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item)));
    }
    public ItemModelBuilder projectileItem(ResourceLocation item) {
        return this.getBuilder(item.toString())
                .parent(new ModelFile.UncheckedModelFile("mkultra:item/standarditem"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(item.getNamespace(), "item/" + item.getPath()));
    }
}
