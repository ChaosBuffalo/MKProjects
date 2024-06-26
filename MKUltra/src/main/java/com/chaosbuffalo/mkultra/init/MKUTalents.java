package com.chaosbuffalo.mkultra.init;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.talents.MKTalent;
import com.chaosbuffalo.mkcore.core.talents.talent_types.PassiveTalent;
import com.chaosbuffalo.mkultra.MKUltra;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class MKUTalents {

    public static DeferredRegister<MKTalent> TALENTS =
            DeferredRegister.create(MKCoreRegistry.TALENT_REGISTRY_NAME, MKUltra.MODID);

    public static RegistryObject<PassiveTalent> SOUL_DRAIN_TALENT = TALENTS.register("soul_drain",
            () -> new PassiveTalent(MKUAbilities.SOUL_DRAIN));

    public static RegistryObject<PassiveTalent> LIFE_SIPHON_TALENT = TALENTS.register("life_siphon",
            () -> new PassiveTalent(MKUAbilities.LIFE_SIPHON));

    public static RegistryObject<PassiveTalent> GREEN_SOUL_TALENT = TALENTS.register("green_soul",
            () -> new PassiveTalent(MKUAbilities.GREEN_SOUL));


    public static void register(IEventBus modBus) {
        TALENTS.register(modBus);
    }
}
