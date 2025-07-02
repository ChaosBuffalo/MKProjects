package com.chaosbuffalo.mkcore.data.providers;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.init.CoreDataMaps;
import com.chaosbuffalo.mkcore.item.ArmorClass;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.DataMapProvider;

import java.util.concurrent.CompletableFuture;

public abstract class ArmorClassDataMapProvider extends DataMapProvider {
    protected HolderLookup.RegistryLookup<ArmorClass> armorClassRegistry;

    protected ArmorClassDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
       armorClassRegistry = provider.lookupOrThrow(MKCoreRegistry.ARMOR_CLASS_REGISTRY_KEY);
       gatherClasses(provider);
    }

    protected abstract void gatherClasses(HolderLookup.Provider provider);

    public void itemTag(TagKey<Item> itemTag, ResourceKey<ArmorClass> classKey) {
        var armorClass = armorClassRegistry.getOrThrow(classKey);

        builder(CoreDataMaps.ARMOR_CLASS_MAPPING)
                .add(itemTag, armorClass,false)
                .build();
    }
}
