package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityMemories;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.chaosbuffalo.mkcore.core.talents.MKTalent;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.init.*;
import com.chaosbuffalo.mkcore.test.MKCoreTestItems;
import com.chaosbuffalo.mkcore.test.MKCoreTestTalents;
import com.chaosbuffalo.mkcore.test.MKTestAbilities;
import com.chaosbuffalo.mkcore.test.MKTestEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class MKCoreRegistry {
    public static final ResourceLocation INVALID_ABILITY = new ResourceLocation(MKCore.MOD_ID, "ability.invalid");
    public static final ResourceLocation INVALID_TALENT = new ResourceLocation(MKCore.MOD_ID, "talent.invalid");
    public static final ResourceLocation INVALID_ENTITLEMENT = new ResourceLocation(MKCore.MOD_ID, "entitlement.invalid");

    public static final ResourceLocation ABILITY_REGISTRY_NAME = MKCore.makeRL("abilities");
    public static final ResourceLocation DAMAGE_TYPE_REGISTRY_NAME = MKCore.makeRL("damage_types");
    public static final ResourceLocation EFFECT_REGISTRY_NAME = MKCore.makeRL("effects");
    public static final ResourceLocation ENTITLEMENT_REGISTRY_NAME = MKCore.makeRL("entitlements");
    public static final ResourceLocation TALENT_REGISTRY_NAME = MKCore.makeRL("talents");

    public static IForgeRegistry<MKAbility> ABILITIES = null;
    public static IForgeRegistry<MKDamageType> DAMAGE_TYPES = null;
    public static IForgeRegistry<MKEffect> EFFECTS = null;
    public static IForgeRegistry<MKTalent> TALENTS = null;
    public static IForgeRegistry<MKEntitlement> ENTITLEMENTS = null;

    @Nullable
    public static MKAbility getAbility(ResourceLocation abilityId) {
        return ABILITIES.getValue(abilityId);
    }

    @Nullable
    public static MKDamageType getDamageType(ResourceLocation damageTypeId) {
        return DAMAGE_TYPES.getValue(damageTypeId);
    }

    @Nullable
    public static MKEntitlement getEntitlement(ResourceLocation entitlementId) {
        return ENTITLEMENTS.getValue(entitlementId);
    }

    @SubscribeEvent
    public static void createRegistries(NewRegistryEvent event) {
        event.create(new RegistryBuilder<MKAbility>()
                .setName(ABILITY_REGISTRY_NAME)
                .setType(MKAbility.class), r -> ABILITIES = r);
        event.create(new RegistryBuilder<MKDamageType>()
                .setName(DAMAGE_TYPE_REGISTRY_NAME)
                .setType(MKDamageType.class), r -> DAMAGE_TYPES = r);
        event.create(new RegistryBuilder<MKEffect>()
                .setName(EFFECT_REGISTRY_NAME)
                .setType(MKEffect.class), r -> EFFECTS = r);
        event.create(new RegistryBuilder<MKTalent>()
                .setName(TALENT_REGISTRY_NAME)
                .setType(MKTalent.class), r -> TALENTS = r);
        event.create(new RegistryBuilder<MKEntitlement>()
                .setName(ENTITLEMENT_REGISTRY_NAME)
                .setType(MKEntitlement.class), r -> ENTITLEMENTS = r);
    }

    public static void register(IEventBus modBus) {
        CoreEffects.register(modBus);
        CoreEntities.register(modBus);
        CoreEntitlements.register(modBus);
        CoreParticles.register(modBus);
        CoreSounds.register(modBus);
        CoreTalents.register(modBus);
        MKAbilityMemories.register(modBus);
        MKTestEffects.register(modBus);
        MKTestAbilities.register(modBus);
        MKCoreTestItems.register(modBus);
        MKCoreTestTalents.register(modBus);
    }
}
