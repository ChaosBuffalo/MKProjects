package com.chaosbuffalo.mkcore.mixins;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Mob.class)
public class MobEntityMixins {

    /**
     * @author kovak
     * @reason disables vanilla entity block breaking since it makes less sense with the poise system
     * <p>
     * Real name maybeDisableShield
     */
    @Overwrite
    private void maybeDisableShield(Player p_233655_1_, ItemStack p_233655_2_, ItemStack p_233655_3_) {

    }
}
