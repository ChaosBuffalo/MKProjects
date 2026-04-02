package com.chaosbuffalo.mkcore.item;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CoreItemComponents {
    public static final DeferredRegister.DataComponents COMPONENT_TYPES = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MKCore.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemGrantedAbility>> ITEM_ABILITY = COMPONENT_TYPES
            .registerComponentType("item_ability", builder -> {
                return builder.persistent(ItemGrantedAbility.CODEC).networkSynchronized(ItemGrantedAbility.STREAM_CODEC);
            });

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AbilitySourceOverride>> ABILITY_SOURCE = COMPONENT_TYPES
            .registerComponentType("ability_source", builder -> {
                return builder.persistent(AbilitySourceOverride.CODEC).networkSynchronized(AbilitySourceOverride.STREAM_CODEC);
            });

    public static void register(IEventBus modBus) {
        COMPONENT_TYPES.register(modBus);
    }
}
