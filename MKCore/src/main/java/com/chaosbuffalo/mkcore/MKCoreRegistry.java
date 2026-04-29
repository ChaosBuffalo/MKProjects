package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityMemories;
import com.chaosbuffalo.mkcore.abilities.client_state.AbilityClientStateTypes;
import com.chaosbuffalo.mkcore.abilities.projectiles.ProjectileCastBehaviorType;
import com.chaosbuffalo.mkcore.abilities.projectiles.ProjectileCastBehaviorTypes;
import com.chaosbuffalo.mkcore.abilities.client_state.AbilityClientStateType;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.core.entitlements.EntitlementType;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.chaosbuffalo.mkcore.core.talents.TalentNodeDisplay;
import com.chaosbuffalo.mkcore.core.talents.TalentTreeDefinition;
import com.chaosbuffalo.mkcore.core.talents.TalentType;
import com.chaosbuffalo.mkcore.formulas.AbilityFormulaType;
import com.chaosbuffalo.mkcore.formulas.AbilityFormulaTypes;
import com.chaosbuffalo.mkcore.init.CoreTalentTypes;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.init.*;
import com.chaosbuffalo.mkcore.item.ArmorClass;
import com.chaosbuffalo.mkcore.item.CoreItemComponents;
import com.chaosbuffalo.mkcore.test.MKCoreTestItems;
import com.chaosbuffalo.mkcore.test.MKTestAbilities;
import com.chaosbuffalo.mkcore.test.MKTestEffects;
import com.chaosbuffalo.mkcore.utils.location.LocationProviderType;
import com.chaosbuffalo.mkcore.utils.location.LocationProviderTypes;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import javax.annotation.Nullable;

@EventBusSubscriber
public class MKCoreRegistry {
    public static final ResourceLocation INVALID_ABILITY = ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "ability.invalid");

    public static final ResourceKey<Registry<MKAbility>> ABILITY_REGISTRY_KEY = ResourceKey.createRegistryKey(MKCore.id("abilities"));
    public static final ResourceKey<Registry<MKDamageType>> DAMAGE_TYPE_REGISTRY_KEY = ResourceKey.createRegistryKey(MKCore.id("damage_types"));
    public static final ResourceKey<Registry<MKEffect>> EFFECT_REGISTRY_KEY = ResourceKey.createRegistryKey(MKCore.id("effects"));
    public static final ResourceKey<Registry<MKEntitlement>> ENTITLEMENT_REGISTRY_KEY = ResourceKey.createRegistryKey(MKCore.id("entitlements"));
    public static final ResourceKey<Registry<EntitlementType<?>>> ENTITLEMENT_TYPE_REGISTRY_KEY = ResourceKey.createRegistryKey(MKCore.id("entitlement_types"));
    public static final ResourceKey<Registry<TalentType<?>>> TALENT_TYPE_REGISTRY_KEY = ResourceKey.createRegistryKey(MKCore.id("talent_types"));
    public static final ResourceKey<Registry<TalentTreeDefinition>> TALENT_TREE_REGISTRY_KEY = ResourceKey.createRegistryKey(MKCore.id("talent_trees"));
    public static final ResourceKey<Registry<TalentNodeDisplay>> TALENT_NODE_DISPLAY_REGISTRY_KEY = ResourceKey.createRegistryKey(MKCore.id("talent_node_display"));
    public static final ResourceKey<Registry<ArmorClass>> ARMOR_CLASS_REGISTRY_KEY = ResourceKey.createRegistryKey(MKCore.id("armor_class"));

    public static final ResourceKey<Registry<LocationProviderType<?>>> LOC_PROVIDER_TYPES_NAME = ResourceKey.createRegistryKey(
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "loc_provider_types"));
    public static final ResourceKey<Registry<ProjectileCastBehaviorType<?>>> CAST_BEHAVIOR_TYPES_NAME = ResourceKey.createRegistryKey(
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "projectile_cast_behavior_types"));
    public static final ResourceKey<Registry<AbilityClientStateType<?>>> CLIENT_STATE_TYPES_NAME = ResourceKey.createRegistryKey(
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "ability_client_state_types"));
    public static final ResourceKey<Registry<AbilityFormulaType<?>>> ABILITY_FORMULA_TYPE_REGISTRY_KEY = ResourceKey.createRegistryKey(
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "ability_formula_types"));

    public static final Registry<MKAbility> ABILITIES = new RegistryBuilder<>(ABILITY_REGISTRY_KEY)
            .sync(true)
            .create();
    public static final Registry<MKDamageType> DAMAGE_TYPES = new RegistryBuilder<>(DAMAGE_TYPE_REGISTRY_KEY)
            .sync(true)
            .create();
    public static final Registry<MKEffect> EFFECTS = new RegistryBuilder<>(EFFECT_REGISTRY_KEY)
            .sync(true)
            .create();
    public static final Registry<TalentType<?>> TALENT_TYPES = new RegistryBuilder<>(TALENT_TYPE_REGISTRY_KEY)
            .sync(true)
            .create();
    public static final Registry<EntitlementType<?>> ENTITLEMENT_TYPES = new RegistryBuilder<>(ENTITLEMENT_TYPE_REGISTRY_KEY)
            .sync(true)
            .create();
    public static final Registry<LocationProviderType<?>> LOCATION_PROVIDER_TYPES= new RegistryBuilder<>(LOC_PROVIDER_TYPES_NAME)
            .sync(true)
            .create();
    public static final Registry<ProjectileCastBehaviorType<?>> CAST_BEHAVIOR_TYPES= new RegistryBuilder<>(CAST_BEHAVIOR_TYPES_NAME)
            .sync(true)
            .create();
    public static final Registry<AbilityClientStateType<?>> CLIENT_STATE_TYPES = new RegistryBuilder<>(CLIENT_STATE_TYPES_NAME)
            .sync(true)
            .create();
    public static final Registry<AbilityFormulaType<?>> ABILITY_FORMULA_TYPES = new RegistryBuilder<>(ABILITY_FORMULA_TYPE_REGISTRY_KEY)
            .sync(true)
            .create();


    @Nullable
    public static MKAbility getAbility(ResourceLocation abilityId) {
        return ABILITIES.get(abilityId);
    }

    @Nullable
    public static MKDamageType getDamageType(ResourceLocation damageTypeId) {
        return DAMAGE_TYPES.get(damageTypeId);
    }

    @SubscribeEvent
    public static void createRegistries(NewRegistryEvent event) {
        event.register(ABILITIES);
        event.register(DAMAGE_TYPES);
        event.register(EFFECTS);
        event.register(TALENT_TYPES);
        event.register(ENTITLEMENT_TYPES);
        event.register(LOCATION_PROVIDER_TYPES);
        event.register(CLIENT_STATE_TYPES);
        event.register(CAST_BEHAVIOR_TYPES);
        event.register(ABILITY_FORMULA_TYPES);
    }

    @SubscribeEvent
    public static void createDataPackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(ARMOR_CLASS_REGISTRY_KEY, ArmorClass.DIRECT_CODEC, ArmorClass.DIRECT_CODEC);
        event.dataPackRegistry(TALENT_NODE_DISPLAY_REGISTRY_KEY, TalentNodeDisplay.CODEC, TalentNodeDisplay.CODEC);
        event.dataPackRegistry(TALENT_TREE_REGISTRY_KEY, TalentTreeDefinition.CODEC, TalentTreeDefinition.CODEC);
        event.dataPackRegistry(ENTITLEMENT_REGISTRY_KEY, MKEntitlement.DIRECT_CODEC, MKEntitlement.DIRECT_CODEC);
    }

    public static void register(IEventBus modBus) {
        CoreCommands.register(modBus);
        CoreDamageTypes.register(modBus);
        CoreEffects.register(modBus);
        CoreEntities.register(modBus);
        CoreEntitlementTypes.register(modBus);
        CoreParticles.register(modBus);
        CoreSounds.register(modBus);
        CoreTalentTypes.register(modBus);
        MKAbilityMemories.register(modBus);
        MKTestEffects.register(modBus);
        MKTestAbilities.register(modBus);
        MKCoreTestItems.register(modBus);
        LocationProviderTypes.register(modBus);
        ProjectileCastBehaviorTypes.register(modBus);
        AbilityClientStateTypes.register(modBus);
        AbilityFormulaTypes.register(modBus);
        MKAttributes.register(modBus);
        CoreAttachments.register(modBus);
        CoreItemComponents.register(modBus);
        CoreDataMaps.register(modBus);
    }
}
