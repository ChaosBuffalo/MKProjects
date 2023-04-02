package com.chaosbuffalo.mkcore.mixins;

import com.chaosbuffalo.mkcore.MKCore;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(LocalPlayer.class)
public abstract class ClientPlayerEntityMixins extends AbstractClientPlayer {

    public ClientPlayerEntityMixins(ClientLevel world, GameProfile profile) {
        super(world, profile);
    }

    @Nonnull
    @Override
    public Collection<MobEffectInstance> getActiveEffects() {
        List<MobEffectInstance> fullList = new ArrayList<>(super.getActiveEffects());
        MKCore.getPlayer(this).ifPresent(playerData -> {
            playerData.getEffects().effects().forEach(effectInstance -> {
                fullList.add(effectInstance.getClientDisplayEffectInstance());
            });
        });

        return fullList;
    }
}
