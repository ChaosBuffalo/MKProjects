package com.chaosbuffalo.mkcore.mixins;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.events.PostAttackEvent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixins {

    @Inject(
            method = "attack(Lnet/minecraft/world/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;resetAttackStrengthTicker()V",
                    shift = At.Shift.AFTER
            )
    )
    private void mkcore$postAttack(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        var playerData = MKCore.getPlayerOrThrow(player);
        playerData.getCombatExtension().recordSwingHit();
        NeoForge.EVENT_BUS.post(new PostAttackEvent(playerData));
    }
}
