package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.talents.MKTalent;
import com.chaosbuffalo.mkcore.core.talents.talent_types.PassiveTalent;
import com.chaosbuffalo.mkcore.core.talents.talent_types.UltimateTalent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class MKCoreTestTalents {

    public static final DeferredRegister<MKTalent> TALENTS =
            DeferredRegister.create(MKCoreRegistry.TALENT_REGISTRY_NAME, MKCore.MOD_ID);

    public static final RegistryObject<PassiveTalent> BURNING_SOUL_PASSIVE = TALENTS.register("talent.burning_soul",
            () -> new PassiveTalent(MKTestAbilities.TEST_NEW_BURNING_SOUL));

    public static final RegistryObject<UltimateTalent> HEALING_RAIN_ULTIMATE = TALENTS.register("talent.healing_rain",
            () -> new UltimateTalent(MKTestAbilities.TEST_HEALING_RAN));


    public static void register(IEventBus modBus) {
        TALENTS.register(modBus);
    }
}
