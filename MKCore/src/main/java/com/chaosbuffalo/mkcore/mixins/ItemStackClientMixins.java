package com.chaosbuffalo.mkcore.mixins;

import com.chaosbuffalo.mkcore.item.AttributeTooltipManager;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackClientMixins {

    @Unique
    private List<Component> tooltipList;

    @Unique
    private Player player;

    @Shadow
    public abstract Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot equipmentSlot);

    // lets us remove attributes only from the automatic tooltip generation in item stack
    @Redirect(
            method = "Lnet/minecraft/world/item/ItemStack;getTooltipLines(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;",
            at = @At(
                    target = "Lnet/minecraft/world/item/ItemStack;getAttributeModifiers(Lnet/minecraft/world/entity/EquipmentSlot;)Lcom/google/common/collect/Multimap;",
                    value = "INVOKE"
            )
    )
    private Multimap<Attribute, AttributeModifier> proxyGetAttributeModifiers(ItemStack itemStack, EquipmentSlot equipmentSlot) {
        // Don't follow our path if it's building the search tree during startup
        if (player == null) {
            return getAttributeModifiers(equipmentSlot);
        }

        if (tooltipList != null && player != null) {
            AttributeTooltipManager.renderTooltip(tooltipList, player, itemStack, equipmentSlot);
        }
        return ImmutableMultimap.of();
    }

    @ModifyVariable(
            method = "Lnet/minecraft/world/item/ItemStack;getTooltipLines(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Player capturePlayer(Player player) {
        this.player = player;
        return player;
    }

    @ModifyVariable(
            method = "Lnet/minecraft/world/item/ItemStack;getTooltipLines(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;",
            at = @At("STORE"),
            index = 3,
            ordinal = 0
    )
    private List<Component> captureList(List<Component> list) {
        this.tooltipList = list;
        return list;
    }
}
