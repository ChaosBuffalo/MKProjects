package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.IMKAbilityProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

import net.minecraft.world.item.Item.Properties;
import org.jetbrains.annotations.Nullable;

public class MKCoreTestItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MKCore.MOD_ID);

    public static RegistryObject<Item> test_armor = ITEMS.register("ability_chest",
            () -> new AbilityArmor(ArmorMaterials.IRON, ArmorItem.Type.CHESTPLATE, new Item.Properties(), MKTestAbilities.TEST_NEW_BURNING_SOUL));


    public static final RegistryObject<Item> ability_sword = ITEMS.register("ability_sword",
            AbilitySword::new);

    public static final RegistryObject<Item> ability_boots = ITEMS.register("ability_boots",
            () -> new AbilityArmor(ArmorMaterials.IRON, ArmorItem.Type.BOOTS, new Item.Properties(), MKTestAbilities.TEST_EMBER));


    public static class AbilityArmor extends ArmorItem implements IMKAbilityProvider {
        private final Supplier<? extends MKAbility> ability;

        public AbilityArmor(ArmorMaterial materialIn, ArmorItem.Type slot, Properties builder, Supplier<? extends MKAbility> ability) {
            super(materialIn, slot, builder);
            this.ability = ability;
        }

        @Nullable
        @Override
        public MKAbilityInfo getAbilityInfo(ItemStack itemStack) {
            return ability.get().getDefaultInstance();
        }
    }

    public static class AbilitySword extends SwordItem implements IMKAbilityProvider {

        public AbilitySword() {
            super(Tiers.IRON, 3, -2.4F, (new Item.Properties()));
        }

        @Nullable
        @Override
        public MKAbilityInfo getAbilityInfo(ItemStack itemStack) {
            return MKTestAbilities.TEST_WHIRLWIND_BLADES.get().getDefaultInstance();
        }
    }


    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
