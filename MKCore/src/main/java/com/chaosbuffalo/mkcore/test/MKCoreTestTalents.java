package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.talents.MKTalent;
import com.chaosbuffalo.mkcore.core.talents.talent_types.PassiveTalent;
import com.chaosbuffalo.mkcore.core.talents.talent_types.UltimateTalent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MKCoreTestTalents {

    public static final DeferredRegister<MKTalent> TALENTS =
            DeferredRegister.create(MKCoreRegistry.TALENT_REGISTRY_KEY, MKCore.MOD_ID);

    public static final DeferredHolder<MKTalent, PassiveTalent> BURNING_SOUL_PASSIVE = TALENTS.register("burning_soul",
            () -> new PassiveTalent(MKTestAbilities.TEST_NEW_BURNING_SOUL));

    public static final DeferredHolder<MKTalent, UltimateTalent> HEALING_RAIN_ULTIMATE = TALENTS.register("healing_rain",
            () -> new UltimateTalent(MKTestAbilities.TEST_HEALING_RAN));


    public static void register(IEventBus modBus) {
        TALENTS.register(modBus);
    }
}
