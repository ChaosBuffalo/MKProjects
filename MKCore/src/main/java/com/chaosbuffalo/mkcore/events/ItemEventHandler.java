package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.utils.ItemUtils;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;

import java.util.UUID;
import java.util.function.Supplier;

@EventBusSubscriber(modid = MKCore.MOD_ID)
public class ItemEventHandler {

    private static final UUID[] SHIELD_EFFICIENCY_MOD_UUID = new UUID[]{
            UUID.fromString("ef26b7ab-f309-4bf6-9b9f-928173c467f1"),
            UUID.fromString("2a83c9cc-ee55-4270-a842-1eb56969d335")
    };

    private static final UUID[] SHIELD_POISE_MOD_UUID = new UUID[]{
            UUID.fromString("b463e341-5c71-4855-966e-a6aa2743d22f"),
            UUID.fromString("94fe11b3-5b65-47f1-ad76-93ba2cd15b6a")
    };

    private static final UUID[] SWORD_EFFICIENCY_MOD_UUID = new UUID[]{
            UUID.fromString("5dabae27-f1a6-4b45-b63e-c6acd8b356a4"),
            UUID.fromString("07c3acc2-82df-4873-8444-d09260e08594")
    };

    private static final UUID[] SWORD_POISE_MOD_UUID = new UUID[]{
            UUID.fromString("8b45f437-5758-482d-80c3-ceddb13d9fe4"),
            UUID.fromString("e5a445c0-a08d-4cf5-960a-0945c505da94")
    };


    private static final UUID CRIT_CHANCE_MODIFIER = UUID.fromString("3935094f-87c5-49a8-bcde-ea29ce3bb5f9");
    private static final UUID CRIT_MULT_MODIFIER = UUID.fromString("c167f8f7-7bfc-4232-a321-ba635a4eb46f");




    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity().getCommandSenderWorld().isClientSide()) {
            return;
        }
        MKCore.getEntityData(event.getEntity()).ifPresent(entityData -> {
            entityData.getEquipment().onEquipmentChange(event.getSlot(), event.getFrom(), event.getTo());
        });

    }

    private static AttributeModifier createDefaultSlotModifier(UUID uuid, double amount, AttributeModifier.Operation op) {
        return new AttributeModifier(ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, uuid.toString()), amount, op);
    }

    private static void addDefaultAttribute(ItemAttributeModifierEvent event, Holder<Attribute> attribute, Supplier<AttributeModifier> modifierSupplier, EquipmentSlotGroup group) {
        if (!event.getModifiers().stream().anyMatch(x -> x.attribute().equals(attribute))) {
            event.addModifier(attribute, modifierSupplier.get(), group);
        }
    }

    @SubscribeEvent
    public static void onItemAttributeModifierEvent(ItemAttributeModifierEvent event) {
        Item from = event.getItemStack().getItem();
        if (from instanceof SwordItem) {
            addDefaultAttribute(event, MKAttributes.MAX_POISE,
                    () -> createDefaultSlotModifier(SWORD_POISE_MOD_UUID[0],
                            20.0,
                            AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);

            addDefaultAttribute(event, MKAttributes.BLOCK_EFFICIENCY,
                    () -> createDefaultSlotModifier(SWORD_EFFICIENCY_MOD_UUID[0],
                            0.75,
                            AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);

            addDefaultAttribute(event, MKAttributes.MELEE_CRIT,
                    () -> createDefaultSlotModifier(CRIT_CHANCE_MODIFIER,
                            ItemUtils.getCritChanceForItem(event.getItemStack()),
                            AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);

            addDefaultAttribute(event, MKAttributes.MELEE_CRIT_MULTIPLIER,
                    () -> createDefaultSlotModifier(CRIT_MULT_MODIFIER,
                            ItemUtils.getCritMultiplierForItem(event.getItemStack()),
                            AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        }
        if (from instanceof ShieldItem) {
            addDefaultAttribute(event, MKAttributes.MAX_POISE,
                    () -> createDefaultSlotModifier(SHIELD_POISE_MOD_UUID[1],
                            50.0,
                            AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.OFFHAND);
            addDefaultAttribute(event, MKAttributes.BLOCK_EFFICIENCY,
                    () -> createDefaultSlotModifier(SHIELD_EFFICIENCY_MOD_UUID[1],
                            1.0f,
                            AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.OFFHAND);
        }
    }
}
