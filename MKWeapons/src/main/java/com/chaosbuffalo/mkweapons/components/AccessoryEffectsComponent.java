package com.chaosbuffalo.mkweapons.components;

import com.chaosbuffalo.mkweapons.items.effects.IItemEffect;
import com.chaosbuffalo.mkweapons.items.effects.ItemModifierEffect;
import com.chaosbuffalo.mkweapons.items.effects.accesory.IAccessoryEffect;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.List;

public record AccessoryEffectsComponent(List<IAccessoryEffect> effects) {
    public static final AccessoryEffectsComponent EMPTY = new AccessoryEffectsComponent(List.of());

    public static final Codec<AccessoryEffectsComponent> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            IAccessoryEffect.DISPATCH_CODEC.listOf().fieldOf("effects").forGetter(AccessoryEffectsComponent::effects)
    ).apply(builder, AccessoryEffectsComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AccessoryEffectsComponent> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public AccessoryEffectsComponent withAddedEffect(IAccessoryEffect newEffect) {
        ImmutableList.Builder<IAccessoryEffect> builder = ImmutableList.builderWithExpectedSize(this.effects.size() + 1);

        for (IAccessoryEffect oldEffect : this.effects) {
            builder.add(oldEffect);
        }

        builder.add(newEffect);
        return new AccessoryEffectsComponent(builder.build());
    }

    public static void addEffect(ItemStack itemStack, IAccessoryEffect effect) {
        itemStack.update(WeaponsComponents.ACCESSORY_EFFECTS, AccessoryEffectsComponent.EMPTY, existing -> existing.withAddedEffect(effect));
    }

    ItemAttributeModifiers generateAttrs(List<? super IItemEffect> effects, EquipmentSlotGroup slotGroup) {
        var builder = ItemAttributeModifiers.builder();

        for (var effect : effects) {
            if (effect instanceof ItemModifierEffect modifierEffect) {
                modifierEffect.getModifiers().forEach(e -> {
                    builder.add(e.getAttribute(), e.getModifier(), slotGroup);
                });
            }
        }

        return builder.build();
    }
}
