package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;

public class CoreTags {
    public static class DamageTypes {
        public static final TagKey<DamageType> MK_MELEE_DAMAGE = tag("mk_melee_damage");
        public static final TagKey<DamageType> MK_PROJECTILE_DAMAGE = tag("mk_projectile_damage");
        public static final TagKey<DamageType> VANILLA_MELEE_DAMAGE = tag("vanilla_melee_damage");

        private static TagKey<DamageType> tag(String name) {
            return TagKey.create(Registries.DAMAGE_TYPE, MKCore.makeRL(name));
        }
    }

    public static class Items {
        public static final TagKey<Item> ARMOR = tag("armor");

        public static final TagKey<Item> ROBES_ARMOR = tag("armor/robes");
        public static final TagKey<Item> LIGHT_ARMOR = tag("armor/light");
        public static final TagKey<Item> MEDIUM_ARMOR = tag("armor/medium");
        public static final TagKey<Item> HEAVY_ARMOR = tag("armor/heavy");

        private static TagKey<Item> tag(String name) {
            return ItemTags.create(MKCore.makeRL(name));
        }
    }
}
