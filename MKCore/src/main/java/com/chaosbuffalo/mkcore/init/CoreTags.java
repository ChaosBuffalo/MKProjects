package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class CoreTags {
    public static class Items {
        public static final TagKey<Item> ARMOR = tag("armor");
        public static final TagKey<Item> LIGHT_ARMOR = tag("armor/light");
        public static final TagKey<Item> MEDIUM_ARMOR = tag("armor/medium");
        public static final TagKey<Item> HEAVY_ARMOR = tag("armor/heavy");

        private static TagKey<Item> tag(String name) {
            return ItemTags.create(MKCore.makeRL(name));
        }
    }
}
