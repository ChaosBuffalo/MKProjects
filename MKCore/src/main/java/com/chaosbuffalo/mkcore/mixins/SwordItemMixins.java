package com.chaosbuffalo.mkcore.mixins;

import com.chaosbuffalo.mkcore.MKCore;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(SwordItem.class)
public abstract class SwordItemMixins extends TieredItem {
    private static final Set<ItemAbility> SWORD_ACTIONS_AMENDED = Stream.of(
            ItemAbilities.SWORD_DIG, ItemAbilities.SWORD_SWEEP, ItemAbilities.SHIELD_BLOCK)
            .collect(Collectors.toCollection(Sets::newIdentityHashSet));


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
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }


    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return SWORD_ACTIONS_AMENDED.contains(itemAbility);
    }

    /**
     * @author kovak
     * @reason make sword block only when we are not poise broke and shield is not in offhand
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof ShieldItem) {
            return InteractionResultHolder.pass(itemstack);
        }
        if (MKCore.getPlayer(player).map(x -> x.getStats().isPoiseBroke()).orElse(false)) {
            return InteractionResultHolder.pass(itemstack);
        } else {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(itemstack);
        }
    }
}
