package com.chaosbuffalo.mkweapons.items.accessories;

import com.chaosbuffalo.mkweapons.components.WeaponsComponents;
import com.chaosbuffalo.mkweapons.items.effects.ItemModifierEffect;
import com.chaosbuffalo.mkweapons.items.effects.accesory.IAccessoryEffect;
import com.chaosbuffalo.mkweapons.items.randomization.options.AttributeOptionEntry;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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
    private static final Multimap<Holder<Attribute>, AttributeModifier> EMPTY_MODIFIERS = ImmutableMultimap.of();

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

    // This is needed to apply the attribute values from the curio, but due to how we use the effects for
    // attributes we also need ItemAttributeModifierEvent in order to display the tooltip
    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
        var stackEffects = getAccessoryEffects(stack);
        if (stackEffects.isEmpty())
            return EMPTY_MODIFIERS;

        Multimap<Holder<Attribute>, AttributeModifier> map = HashMultimap.create();
        for (var effect : stackEffects) {
            if (effect instanceof ItemModifierEffect modifierEffect) {
                for (AttributeOptionEntry m : modifierEffect.getModifiers()) {
                    // Give each modifier a unique id reflecting their curio slot
                    var mod = m.getModifierWithId(modifierId -> modifierId.withSuffix("/" + id.toLanguageKey()));

                    map.put(m.getAttribute(), mod);
                }
            }
        }
        return map;
    }

    @Override
    public boolean needsAttributesEventSupport() {
        return true;
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