package com.chaosbuffalo.mkcore.mixins;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixins {

    @Inject(
            method = "readAdditionalSaveData",
            at = @At("HEAD")
    )
    public void mkcore$readAdditionalData(CompoundTag compound, CallbackInfo ci) {
        float mkcore$originalHealth = compound.getFloat("Health");
        ServerPlayer self = (ServerPlayer) (Object) this;
        self.getPersistentData().putFloat("mkcore$SavedHealth", mkcore$originalHealth);
    }
}
