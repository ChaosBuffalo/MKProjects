package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.talents.MKTalent;
import com.chaosbuffalo.mkcore.core.talents.talent_types.PassiveTalent;
import com.chaosbuffalo.mkcore.core.talents.talent_types.UltimateTalent;
import net.minecraft.core.Holder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MKCoreTestTalents {

    public static final DeferredRegister<MKTalent> TALENTS =
            DeferredRegister.create(MKCoreRegistry.TALENT_REGISTRY_KEY, MKCore.MOD_ID);

    public static final Holder<MKTalent> BURNING_SOUL_PASSIVE = TALENTS.register("burning_soul",
            () -> new PassiveTalent(MKTestAbilities.TEST_NEW_BURNING_SOUL));

    public static final Holder<MKTalent> HEALING_RAIN_ULTIMATE = TALENTS.register("healing_rain",
            () -> new UltimateTalent(MKTestAbilities.TEST_HEALING_RAN));


    public static void register(IEventBus modBus) {
        TALENTS.register(modBus);
    }
}
