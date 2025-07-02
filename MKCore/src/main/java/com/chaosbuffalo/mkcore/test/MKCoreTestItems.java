package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.item.CoreItemComponents;
import com.chaosbuffalo.mkcore.item.ItemGrantedAbility;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MKCoreTestItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, MKCore.MOD_ID);

    public static DeferredHolder<Item, AbilityArmor> test_armor = ITEMS.register("ability_chest",
            () -> new AbilityArmor(ArmorMaterials.IRON, ArmorItem.Type.CHESTPLATE, new Item.Properties(), MKTestAbilities.TEST_NEW_BURNING_SOUL));


    public static final DeferredHolder<Item, AbilitySword> ability_sword = ITEMS.register("ability_sword",
            AbilitySword::new);

    public static final DeferredHolder<Item, AbilityArmor> ability_boots = ITEMS.register("ability_boots",
            () -> new AbilityArmor(ArmorMaterials.IRON, ArmorItem.Type.BOOTS, new Item.Properties(), MKTestAbilities.TEST_EMBER));


    public static class AbilityArmor extends ArmorItem {

        public AbilityArmor(Holder<ArmorMaterial> materialIn, ArmorItem.Type slot, Properties builder, Holder<MKAbility> ability) {
            super(materialIn, slot, builder
                    .component(CoreItemComponents.ITEM_ABILITY, new ItemGrantedAbility(ability)));

        }
    }

    public static class AbilitySword extends SwordItem {

        public AbilitySword() {
            super(Tiers.IRON, (new Item.Properties()
                    .component(CoreItemComponents.ITEM_ABILITY, new ItemGrantedAbility(MKTestAbilities.TEST_WHIRLWIND_BLADES))));
        }
    }


    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
