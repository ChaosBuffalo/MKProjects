package com.chaosbuffalo.mkweapons.items.accessories;

import com.chaosbuffalo.mkweapons.items.effects.accesory.IAccessoryEffect;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public interface IMKAccessory {
    List<? extends IAccessoryEffect> getAccessoryEffects(ItemStack item);

    void addToTooltip(ItemStack stack, @Nullable Player player, List<Component> tooltip);
}
