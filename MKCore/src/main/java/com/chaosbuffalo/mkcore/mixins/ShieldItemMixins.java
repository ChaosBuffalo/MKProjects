package com.chaosbuffalo.mkcore.mixins;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ShieldItem.class)
public abstract class ShieldItemMixins {

    /**
     * @author kovak
     * @reason shield can't block if we're poise broke
     */
    @Overwrite
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (MKCore.getPlayer(player).map(x -> x.getStats().isPoiseBroke()).orElse(false)) {
            return InteractionResultHolder.pass(itemstack);
        } else {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(itemstack);
        }
    }
}
