package com.chaosbuffalo.mkcore.data.content;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.init.CoreArmorClasses;
import com.chaosbuffalo.mkcore.init.CoreEntitlements;
import com.chaosbuffalo.mkcore.init.CoreTalentDisplayNodes;
import com.chaosbuffalo.mkcore.init.CoreTalentTrees;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class CoreRegistrySets extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(MKCoreRegistry.ARMOR_CLASS_REGISTRY_KEY, CoreArmorClasses::bootstrap)
            .add(MKCoreRegistry.TALENT_NODE_DISPLAY_REGISTRY_KEY, CoreTalentDisplayNodes::bootstrap)
            .add(MKCoreRegistry.TALENT_TREE_REGISTRY_KEY, CoreTalentTrees::bootstrap)
            .add(MKCoreRegistry.ENTITLEMENT_REGISTRY_KEY, CoreEntitlements::bootstrap);


    public CoreRegistrySets(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(MKCore.MOD_ID));
    }
}
