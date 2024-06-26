package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityMemories;
import com.chaosbuffalo.mkcore.abilities.client_state.AbilityClientStateTypes;
import com.chaosbuffalo.mkcore.abilities.projectiles.ProjectileCastBehaviorType;
import com.chaosbuffalo.mkcore.abilities.projectiles.ProjectileCastBehaviorTypes;
import com.chaosbuffalo.mkcore.abilities.client_state.AbilityClientStateType;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.chaosbuffalo.mkcore.core.talents.MKTalent;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.init.*;
import com.chaosbuffalo.mkcore.test.MKCoreTestItems;
import com.chaosbuffalo.mkcore.test.MKCoreTestTalents;
import com.chaosbuffalo.mkcore.test.MKTestAbilities;
import com.chaosbuffalo.mkcore.test.MKTestEffects;
import com.chaosbuffalo.mkcore.utils.location.LocationProviderType;
import com.chaosbuffalo.mkcore.utils.location.LocationProviderTypes;
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

    public static final ResourceLocation LOC_PROVIDER_TYPES_NAME = new ResourceLocation(MKCore.MOD_ID, "loc_provider_types");
    public static IForgeRegistry<LocationProviderType<?>> LOC_PROVIDER_TYPES = null;
    public static final ResourceLocation CAST_BEHAVIOR_TYPES_NAME = new ResourceLocation(MKCore.MOD_ID, "projectile_cast_behavior_types");
    public static IForgeRegistry<ProjectileCastBehaviorType<?>> PROJECTILE_CAST_BEHAVIOR_TYPES = null;
    public static final ResourceLocation CLIENT_STATE_TYPES_NAME = new ResourceLocation(MKCore.MOD_ID, "ability_client_state_types");
    public static IForgeRegistry<AbilityClientStateType<?>> CLIENT_STATE_TYPES = null;

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
                .setName(ABILITY_REGISTRY_NAME), r -> ABILITIES = r);
        event.create(new RegistryBuilder<MKDamageType>()
                .setName(DAMAGE_TYPE_REGISTRY_NAME), r -> DAMAGE_TYPES = r);
        event.create(new RegistryBuilder<MKEffect>()
                .setName(EFFECT_REGISTRY_NAME), r -> EFFECTS = r);
        event.create(new RegistryBuilder<MKTalent>()
                .setName(TALENT_REGISTRY_NAME), r -> TALENTS = r);
        event.create(new RegistryBuilder<MKEntitlement>()
                .setName(ENTITLEMENT_REGISTRY_NAME), r -> ENTITLEMENTS = r);
        event.create(new RegistryBuilder<LocationProviderType<?>>()
                .setName(LOC_PROVIDER_TYPES_NAME), r -> LOC_PROVIDER_TYPES = r);
        event.create(new RegistryBuilder<ProjectileCastBehaviorType<?>>()
                .setName(CAST_BEHAVIOR_TYPES_NAME), r -> PROJECTILE_CAST_BEHAVIOR_TYPES = r);
        event.create(new RegistryBuilder<AbilityClientStateType<?>>()
                .setName(CLIENT_STATE_TYPES_NAME), r -> CLIENT_STATE_TYPES = r);
    }

    public static void register(IEventBus modBus) {
        CoreCommands.register(modBus);
        CoreDamageTypes.register(modBus);
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
        LocationProviderTypes.register(modBus);
        ProjectileCastBehaviorTypes.register(modBus);
        AbilityClientStateTypes.register(modBus);

    }
}
