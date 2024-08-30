package com.chaosbuffalo.mkweapons.items.armor;

import com.chaosbuffalo.mkweapons.components.ArmorEffectsComponent;
import com.chaosbuffalo.mkweapons.components.WeaponsComponents;
import com.chaosbuffalo.mkweapons.items.effects.armor.IArmorEffect;
import com.google.common.base.Suppliers;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.common.util.ConcatenatedListView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class MKArmorItem extends ArmorItem implements IMKArmor {
    private final List<IArmorEffect> armorEffects;
    private final Supplier<ItemAttributeModifiers> defaultModifiers;

    public MKArmorItem(Holder<ArmorMaterial> materialIn, ArmorItem.Type type, Properties builderIn,
                       IArmorEffect... armorEffects) {
        super(materialIn, type, builderIn);

        this.defaultModifiers = Suppliers.memoize(() -> {
            int defense = material.value().getDefense(getType());
            float toughness = material.value().toughness();
            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
            EquipmentSlotGroup slotGroup = EquipmentSlotGroup.bySlot(getType().getSlot());
            ResourceLocation modifierId = ResourceLocation.withDefaultNamespace("armor." + getType().getName());
            builder.add(Attributes.ARMOR, new AttributeModifier(modifierId, defense, AttributeModifier.Operation.ADD_VALUE), slotGroup);
            builder.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(modifierId, toughness, AttributeModifier.Operation.ADD_VALUE), slotGroup);
            float resistance = material.value().knockbackResistance();
            if (resistance > 0.0F) {
                builder.add(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(modifierId, resistance, AttributeModifier.Operation.ADD_VALUE), slotGroup);
            }

            buildAttributes(builder, modifierId, slotGroup);
            return builder.build();
        });

        this.armorEffects = Arrays.asList(armorEffects);
    }

    protected void buildAttributes(ItemAttributeModifiers.Builder builder, ResourceLocation modifierId,
                                   EquipmentSlotGroup slot) {

    }

    @Nonnull
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        return this.defaultModifiers.get();
    }

    public void addToTooltip(ItemStack stack, @Nullable Player player, List<Component> tooltip) {
        for (IArmorEffect armorEffect : getArmorEffects(stack)) {
            armorEffect.addInformation(stack, player, tooltip);
        }
    }

    @Override
    public List<IArmorEffect> getArmorEffects(ItemStack item) {
        ArmorEffectsComponent stackEffects = item.get(WeaponsComponents.ARMOR_EFFECTS);
        if (stackEffects != null) {
            return ConcatenatedListView.of(armorEffects, stackEffects.effects());
        } else {
            return armorEffects;
        }
    }
}
