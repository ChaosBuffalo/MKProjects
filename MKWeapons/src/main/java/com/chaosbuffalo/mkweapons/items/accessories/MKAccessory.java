package com.chaosbuffalo.mkweapons.items.accessories;

import com.chaosbuffalo.mkweapons.capabilities.MKCurioItemHandler;
import com.chaosbuffalo.mkweapons.components.WeaponsComponents;
import com.chaosbuffalo.mkweapons.items.effects.accesory.IAccessoryEffect;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.common.util.ConcatenatedListView;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


public class MKAccessory extends Item implements ICurioItem {

    private final List<IAccessoryEffect> effects;

    public MKAccessory(Properties properties, IAccessoryEffect... effectsIn) {
        super(properties);
        effects = new ArrayList<>();
        effects.addAll(Arrays.asList(effectsIn));
    }

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

    public List<? extends IAccessoryEffect> getAccessoryEffects() {
        return effects;
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

    public void addToTooltip(ItemStack stack, @Nullable Player player, List<Component> tooltip) {
        for (IAccessoryEffect accessoryEffect : getAccessoryEffects(stack)) {
            accessoryEffect.addInformation(stack, player, tooltip);
        }
    }

    public static Optional<MKCurioItemHandler> getAccessoryHandler(ItemStack item) {
        ICurio curioCap = item.getCapability(CuriosCapability.ITEM);
        if (curioCap instanceof MKCurioItemHandler handler) {
            return Optional.of(handler);
        }
        return Optional.empty();
    }

    public static List<MKCurioItemHandler> getMKCurios(LivingEntity entity) {
        List<MKCurioItemHandler> curios = new ArrayList<>();
        CuriosApi.getCuriosHelper().getEquippedCurios(entity).ifPresent(x -> {
            for (int i = 0; i < x.getSlots(); i++) {
                ItemStack curioIS = x.getStackInSlot(i);
                if (!curioIS.isEmpty() && curioIS.getItem() instanceof MKAccessory) {
                    MKAccessory.getAccessoryHandler(curioIS).ifPresent(curios::add);
                }
            }
        });
        return curios;
    }
}