package com.chaosbuffalo.mkweapons.mixins;


import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public class PlayerAttackMixins {

    // disables reset cooldown as we handle it ourselves later
    @Redirect(at = @At(value= "INVOKE", target="Lnet/minecraft/world/entity/player/Player;resetAttackStrengthTicker()V"),
        method = "Lnet/minecraft/world/entity/player/Player;attack(Lnet/minecraft/world/entity/Entity;)V")
    private void proxyResetAttackStrengthTicker(Player entity){

    }
}
