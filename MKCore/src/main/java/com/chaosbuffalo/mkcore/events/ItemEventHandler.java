package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
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

import java.util.function.Function;

@EventBusSubscriber(modid = MKCore.MOD_ID)
public class ItemEventHandler {

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        IMKEntityData entityData = MKCore.getEntityDataOrThrow(event.getEntity());
        entityData.getEquipment().onEquipmentChange(event.getSlot(), event.getFrom(), event.getTo());
    }

    private static AttributeModifier createDefaultSlotModifier(String id, double amount, AttributeModifier.Operation op) {
        return new AttributeModifier(ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, id), amount, op);
    }

    private static void addDefaultAttribute(ItemAttributeModifierEvent event, Holder<Attribute> attribute, Function<String,AttributeModifier> modifierSupplier, EquipmentSlotGroup group) {
        if (event.getModifiers().stream().noneMatch(x -> x.attribute().equals(attribute))) {
            String id = "implicit." + group.getSerializedName();
            event.addModifier(attribute, modifierSupplier.apply(id), group);
        }
    }

    @SubscribeEvent
    public static void onItemAttributeModifierEvent(ItemAttributeModifierEvent event) {
        Item from = event.getItemStack().getItem();
        if (from instanceof SwordItem) {
            addDefaultAttribute(event, MKAttributes.MAX_POISE,
                    id -> createDefaultSlotModifier(id,
                            20.0,
                            AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);

            addDefaultAttribute(event, MKAttributes.BLOCK_EFFICIENCY,
                    id -> createDefaultSlotModifier(id,
                            0.75,
                            AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);

            addDefaultAttribute(event, MKAttributes.MELEE_CRIT,
                    id -> createDefaultSlotModifier(id,
                            ItemUtils.getCritChanceForItem(event.getItemStack()),
                            AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);

            addDefaultAttribute(event, MKAttributes.MELEE_CRIT_MULTIPLIER,
                    id -> createDefaultSlotModifier(id,
                            ItemUtils.getCritMultiplierForItem(event.getItemStack()),
                            AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        }
        if (from instanceof ShieldItem) {
            addDefaultAttribute(event, MKAttributes.MAX_POISE,
                    id -> createDefaultSlotModifier(id,
                            50.0,
                            AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.OFFHAND);
            addDefaultAttribute(event, MKAttributes.BLOCK_EFFICIENCY,
                    id -> createDefaultSlotModifier(id,
                            1.0f,
                            AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.OFFHAND);
        }
    }
}
