package com.chaosbuffalo.mkcore.mixins.client;

import com.chaosbuffalo.mkcore.item.AttributeTooltipManager;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackClientMixins {

    @Inject(
            method = "addModifierTooltip(Ljava/util/function/Consumer;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void mkcore_addModifierTooltip(Consumer<Component> tooltipAdder, @Nullable Player player,
                                           Holder<Attribute> attribute, AttributeModifier modifier, CallbackInfo ci) {
        if (player == null) {
            return;
        }

        final ItemStack stack = (ItemStack) (Object) this;
        if (AttributeTooltipManager.renderModifier(player, stack, attribute, modifier, tooltipAdder)) {
            ci.cancel();
        }
    }
}
