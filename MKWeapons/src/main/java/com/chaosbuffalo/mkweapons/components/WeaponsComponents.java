package com.chaosbuffalo.mkweapons.components;

import com.chaosbuffalo.mkweapons.MKWeapons;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class WeaponsComponents {
    public static final DeferredRegister.DataComponents COMPONENT_TYPES = DeferredRegister.createDataComponents(MKWeapons.MODID);


    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MeleeEffectsComponent>> MELEE_EFFECTS = COMPONENT_TYPES
            .registerComponentType("melee_effects", builder -> {
                return builder.persistent(MeleeEffectsComponent.CODEC).networkSynchronized(MeleeEffectsComponent.STREAM_CODEC);
            });

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RangedEffectsComponent>> RANGED_EFFECTS = COMPONENT_TYPES
            .registerComponentType("ranged_effects", builder -> {
                return builder.persistent(RangedEffectsComponent.CODEC).networkSynchronized(RangedEffectsComponent.STREAM_CODEC);
            });

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ArmorEffectsComponent>> ARMOR_EFFECTS = COMPONENT_TYPES
            .registerComponentType("armor_effects", builder -> {
                return builder.persistent(ArmorEffectsComponent.CODEC).networkSynchronized(ArmorEffectsComponent.STREAM_CODEC);
            });

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AccessoryEffectsComponent>> ACCESSORY_EFFECTS = COMPONENT_TYPES
            .registerComponentType("accessory_effects", builder -> {
                return builder.persistent(AccessoryEffectsComponent.CODEC).networkSynchronized(AccessoryEffectsComponent.STREAM_CODEC);
            });

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WeaponAbilityComponent>> WEAPON_ABILITY = COMPONENT_TYPES
            .registerComponentType("weapon_ability", builder -> {
                return builder.persistent(WeaponAbilityComponent.CODEC).networkSynchronized(WeaponAbilityComponent.STREAM_CODEC);
            });


    public static void register(IEventBus modBus) {
        COMPONENT_TYPES.register(modBus);
    }
}
