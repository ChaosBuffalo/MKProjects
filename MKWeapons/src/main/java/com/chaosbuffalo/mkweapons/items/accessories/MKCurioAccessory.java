package com.chaosbuffalo.mkweapons.items.accessories;

import com.chaosbuffalo.mkweapons.components.WeaponsComponents;
import com.chaosbuffalo.mkweapons.items.effects.accesory.IAccessoryEffect;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.ConcatenatedListView;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MKCurioAccessory extends Item implements ICurioItem, IMKAccessory {

    private final List<IAccessoryEffect> effects;

    public MKCurioAccessory(Properties properties, IAccessoryEffect... effectsIn) {
        super(properties);
        effects = new ArrayList<>();
        effects.addAll(Arrays.asList(effectsIn));
    }

    @Override
    public List<? extends IAccessoryEffect> getAccessoryEffects(ItemStack item) {
        var stackComp = item.get(WeaponsComponents.ACCESSORY_EFFECTS);
        if (stackComp != null) {
            return ConcatenatedListView.of(
                    stackComp.effects(),
                    effects
            );
        } else {
            return effects;
        }
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        for (IAccessoryEffect effect : getAccessoryEffects(stack)) {
            effect.onEntityEquip(slotContext.entity());
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        for (IAccessoryEffect effect : getAccessoryEffects(stack)) {
            effect.onEntityUnequip(slotContext.entity());
        }
    }

    @Override
    public void addToTooltip(ItemStack stack, @Nullable Player player, List<Component> tooltip) {
        for (IAccessoryEffect accessoryEffect : getAccessoryEffects(stack)) {
            accessoryEffect.addInformation(stack, player, tooltip);
        }
    }
}