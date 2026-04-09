package com.chaosbuffalo.mkcore.mixins;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixins {
    @Inject(method = "attack(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void mkcore$queueAttackDuringCooldown(Entity target, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (player.level().isClientSide) {
            return;
        }
        var playerData = MKCore.getPlayerOrThrow(player);
        var combat = playerData.getCombatExtension();
        if (!combat.usesCustomMainhandMelee() || !combat.shouldQueueAttack(InteractionHand.MAIN_HAND)) {
            return;
        }
        combat.queueAttack(target, InteractionHand.MAIN_HAND);
        ci.cancel();
    }

    @Inject(
            method = "attack(Lnet/minecraft/world/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;resetAttackStrengthTicker()V",
                    shift = At.Shift.AFTER
            )
    )
    private void mkcore$postAttack(Entity target, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        var playerData = MKCore.getPlayerOrThrow(player);
        var combat = playerData.getCombatExtension();
        var attackHand = combat.getActiveAttackHand();
        boolean secondaryAttack = combat.isExecutingMultiAttack(attackHand);
        combat.setAttackStrengthTicks(attackHand, 0);
        if (player.level().isClientSide) {
            combat.onLocalPrimaryAttackCommitted(target, attackHand);
            return;
        }
        combat.onServerAttackCommitted(target, attackHand, secondaryAttack);
    }
}
