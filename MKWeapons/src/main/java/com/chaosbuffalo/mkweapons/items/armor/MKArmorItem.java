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

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class MKArmorItem extends ArmorItem implements IMKArmor {
    public static final UUID CHEST_UUID = UUID.fromString("77ab4b54-5885-4f7f-ab41-71af536309d1");
    public static final UUID LEGGINGS_UUID = UUID.fromString("8d827c58-8f61-4c77-8dcd-f62f0e69121b");
    public static final UUID HELMET_UUID = UUID.fromString("fb16408c-0421-4138-a283-8da7038e5970");
    public static final UUID FEET_UUID = UUID.fromString("fb16408c-0421-4138-a283-8da7038e5972");
    public static final UUID[] ARMOR_MODIFIERS = new UUID[]{FEET_UUID, LEGGINGS_UUID, CHEST_UUID, HELMET_UUID};
    private final List<IArmorEffect> armorEffects;

    //    private final Multimap<Attribute, AttributeModifier> attributeMap;
    private final Supplier<ItemAttributeModifiers> defaultModifiers;


    public MKArmorItem(Holder<ArmorMaterial> materialIn, ArmorItem.Type type, Properties builderIn,
                       IArmorEffect... armorEffects) {
        super(materialIn, type, builderIn);
//        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
//        UUID uuid = ARMOR_MODIFIERS[type.getSlot().getIndex()];
//        builder.put(Attributes.ARMOR, new AttributeModifier(uuid, "Armor modifier", getDefense(),
//                AttributeModifier.Operation.ADD_VALUE));
//        builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(uuid, "Armor toughness", getToughness(),
//                AttributeModifier.Operation.ADDITION));
//        if (this.knockbackResistance > 0) {
//            builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(uuid, "Armor knockback resistance",
//                    knockbackResistance, AttributeModifier.Operation.ADDITION));
//        }
//        buildAttributes(builder, type.getSlot());
//        this.attributeMap = builder.build();


        this.defaultModifiers = Suppliers.memoize(() -> {
            int defense = material.value().getDefense(type);
            float toughness = material.value().toughness();
            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
            EquipmentSlotGroup slotGroup = EquipmentSlotGroup.bySlot(type.getSlot());
            ResourceLocation modifierId = ResourceLocation.withDefaultNamespace("armor." + type.getName());
            builder.add(Attributes.ARMOR, new AttributeModifier(modifierId, defense, AttributeModifier.Operation.ADD_VALUE), slotGroup);
            builder.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(modifierId, toughness, AttributeModifier.Operation.ADD_VALUE), slotGroup);
            float resistance = material.value().knockbackResistance();
            if (resistance > 0.0F) {
                builder.add(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(modifierId, resistance, AttributeModifier.Operation.ADD_VALUE), slotGroup);
            }

            buildAttributes(builder, slotGroup);

            return builder.build();
        });


        this.armorEffects = Arrays.asList(armorEffects);
    }

    protected void buildAttributes(ItemAttributeModifiers.Builder builder, EquipmentSlotGroup slot) {

    }

    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        return this.defaultModifiers.get();
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        return super.getDefaultAttributeModifiers(stack);
    }

    public void addToTooltip(ItemStack stack, @Nullable Player player, List<Component> tooltip) {
        for (IArmorEffect armorEffect : getArmorEffects(stack)) {
            armorEffect.addInformation(stack, player, tooltip);
        }
    }

    @Override
    public List<IArmorEffect> getArmorEffects() {
        return armorEffects;
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
