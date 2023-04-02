package com.chaosbuffalo.mkcore.mixins;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SwordItem.class)
public abstract class SwordItemMixins extends TieredItem {

    public SwordItemMixins(Tier tierIn, Properties builder) {
        super(tierIn, builder);
    }

    /**
     * @author kovak
     * @reason change sword to block when used
     */
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    /**
     * @author kovak
     * @reason give use duration same as shield
     */
    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }


    /**
     * @author kovak
     * @reason make sword block only when we are not poise broke and shield is not in offhand
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack itemstack = playerIn.getItemInHand(handIn);
        ItemStack offhand = playerIn.getOffhandItem();
        if (offhand.getItem() instanceof ShieldItem) {
            return InteractionResultHolder.pass(itemstack);
        }
        if (MKCore.getPlayer(playerIn).map(x -> x.getStats().isPoiseBroke()).orElse(false)) {
            return InteractionResultHolder.pass(itemstack);
        } else {
            playerIn.startUsingItem(handIn);
            return InteractionResultHolder.consume(itemstack);
        }
    }
}
