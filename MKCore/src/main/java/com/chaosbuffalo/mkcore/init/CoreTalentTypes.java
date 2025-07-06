package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.talents.TalentType;
import com.chaosbuffalo.mkcore.core.talents.nodes.AbilityGrantTalentNode;
import com.chaosbuffalo.mkcore.core.talents.nodes.AttributeTalentNode;
import com.chaosbuffalo.mkcore.core.talents.nodes.EntitlementGrantTalentNode;
import com.chaosbuffalo.mkcore.core.talents.talent_types.AbilityGrantTalentType;
import com.chaosbuffalo.mkcore.core.talents.talent_types.AttributeTalentType;
import com.chaosbuffalo.mkcore.core.talents.talent_types.EntitlementGrantTalentType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CoreTalentTypes {
    public static final DeferredRegister<TalentType<?>> REGISTRY = DeferredRegister.create(MKCoreRegistry.TALENT_TYPE_REGISTRY_KEY, MKCore.MOD_ID);

    public static final DeferredHolder<TalentType<?>, TalentType<AttributeTalentNode>> ATTRIBUTE = REGISTRY.register("attribute",
            AttributeTalentType::new);

    public static final DeferredHolder<TalentType<?>, TalentType<AbilityGrantTalentNode>> ABILITY_GRANT = REGISTRY.register("ability_grant",
            AbilityGrantTalentType::new);

    public static final DeferredHolder<TalentType<?>, TalentType<EntitlementGrantTalentNode>> ENTITLEMENT_GRANT = REGISTRY.register("entitlement_grant",
            EntitlementGrantTalentType::new);


    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
