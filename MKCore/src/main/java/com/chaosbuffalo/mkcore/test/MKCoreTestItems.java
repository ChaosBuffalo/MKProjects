package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKAbilityProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class MKCoreTestItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, MKCore.MOD_ID);

    public static DeferredHolder<Item, AbilityArmor> test_armor = ITEMS.register("ability_chest",
            () -> new AbilityArmor(ArmorMaterials.IRON, ArmorItem.Type.CHESTPLATE, new Item.Properties(), MKTestAbilities.TEST_NEW_BURNING_SOUL));


    public static final DeferredHolder<Item, AbilitySword> ability_sword = ITEMS.register("ability_sword",
            AbilitySword::new);

    public static final DeferredHolder<Item, AbilityArmor> ability_boots = ITEMS.register("ability_boots",
            () -> new AbilityArmor(ArmorMaterials.IRON, ArmorItem.Type.BOOTS, new Item.Properties(), MKTestAbilities.TEST_EMBER));


    public static class AbilityArmor extends ArmorItem implements IMKAbilityProvider {
        private final Supplier<? extends MKAbility> ability;

        public AbilityArmor(Holder<ArmorMaterial> materialIn, ArmorItem.Type slot, Properties builder, Supplier<? extends MKAbility> ability) {
            super(materialIn, slot, builder);
            this.ability = ability;
        }

        @Override
        public MKAbility getAbility(ItemStack item) {
            return ability.get();
        }
    }

    public static class AbilitySword extends SwordItem implements IMKAbilityProvider {

        public AbilitySword() {
            super(Tiers.IRON, (new Item.Properties()));
        }

        @Override
        public MKAbility getAbility(ItemStack item) {
            return MKTestAbilities.TEST_WHIRLWIND_BLADES.get();
        }
    }


    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
