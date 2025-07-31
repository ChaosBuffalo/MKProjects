package com.chaosbuffalo.mkultra.init;

import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcRegistries;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.data.generators.npc.*;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

public class MKUNpcs {

    public static ResourceKey<NpcDefinition> key(String name) {
        return ResourceKey.create(NpcRegistries.NPC_DEFINITIONS, MKUltra.id(name));
    }

    public static void bootstrap(BootstrapContext<NpcDefinition> context) {
        ClericNpcs.bootstrap(context);
        DecayingChurchNpcs.bootstrap(context);
        NecrotideNpcs.bootstrap(context);
        SeawovenNpcs.bootstrap(context);
        GreenKnightNpcs.bootstrap(context);
        HyboreanNpcs.bootstrap(context);
        ThemcromancerNpcs.bootstrap(context);
        IntroCastleNpcs.bootstrap(context);
    }
}
