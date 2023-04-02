package com.chaosbuffalo.mkcore.init;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class CoreItems {

    public static void registerItemProperties() {
        List<Item> swordsToAddBlocking = new ArrayList<>();
        swordsToAddBlocking.add(Items.DIAMOND_SWORD);
        swordsToAddBlocking.add(Items.WOODEN_SWORD);
        swordsToAddBlocking.add(Items.STONE_SWORD);
        swordsToAddBlocking.add(Items.IRON_SWORD);
        swordsToAddBlocking.add(Items.GOLDEN_SWORD);
        swordsToAddBlocking.add(Items.NETHERITE_SWORD);
        for (Item sword : swordsToAddBlocking) {

            //ItemStack p_174676_, @Nullable ClientLevel p_174677_, @Nullable LivingEntity p_174678_, int p_174679_
            ItemProperties.register(sword, new ResourceLocation("blocking"),
                    (itemStack, world, entity, p_174679_) -> entity != null && entity.isUsingItem()
                            && entity.getUseItem() == itemStack ? 1.0F : 0.0F);
        }


    }
}
