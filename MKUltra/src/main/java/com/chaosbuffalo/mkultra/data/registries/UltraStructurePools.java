package com.chaosbuffalo.mkultra.data.registries;

import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.world.gen.feature.structure.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class UltraStructurePools {
    public static ResourceKey<StructureTemplatePool> createKey(String pName) {
        return ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation(MKUltra.MODID, pName));
    }

    public static void register(BootstapContext<StructureTemplatePool> pContext, String pName, StructureTemplatePool pValue) {
        pContext.register(createKey(pName), pValue);
    }

    public static void bootstrap(BootstapContext<StructureTemplatePool> pContext) {
        IntroCastlePools.registerPools(pContext);
        NecrotideAlterPools.registerPools(pContext);
        DesertTempleVillagePools.registerPools(pContext);
        CryptStructurePools.registerPools(pContext);
        DeepslateObeliskPools.registerPools(pContext);
        DecayingChurchPools.registerPools(pContext);
    }
}
