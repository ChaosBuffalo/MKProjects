package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.item.ItemBlockStats;
import com.chaosbuffalo.mkcore.item.ItemCriticalStats;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.core.Holder;
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

import java.util.Set;

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

    private static void addDefaultAttribute(ItemAttributeModifierEvent event, Set<Holder<Attribute>> existing,
                                            Holder<Attribute> attribute, double amount,
                                            AttributeModifier.Operation op, EquipmentSlotGroup group) {
        if (!existing.contains(attribute)) {
            var modId = MKCore.id("implicit." + op.id() + "." + group.getSerializedName());
            event.addModifier(attribute, new AttributeModifier(modId, amount, op), group);
        }
    }

    @SubscribeEvent
    public static void onItemAttributeModifierEvent(ItemAttributeModifierEvent event) {
        Item from = event.getItemStack().getItem();
        if (from instanceof SwordItem) {
            var weaponMods = event.getModifiers();
            Set<Holder<Attribute>> existing = new ObjectArraySet<>(weaponMods.size());
            for (var entry : weaponMods) {
                existing.add(entry.attribute());
            }

            var blockStats = ItemBlockStats.get(event.getItemStack());
            if (blockStats != null) {
                addDefaultAttribute(event, existing, MKAttributes.MAX_POISE,
                        blockStats.maxPoise(), AttributeModifier.Operation.ADD_VALUE, EquipmentSlotGroup.MAINHAND);
                addDefaultAttribute(event, existing, MKAttributes.BLOCK_EFFICIENCY,
                        blockStats.blockEfficiency(), AttributeModifier.Operation.ADD_VALUE, EquipmentSlotGroup.MAINHAND);
            }

            var critStats = ItemCriticalStats.getOrDefault(event.getItemStack());
            addDefaultAttribute(event, existing, MKAttributes.MELEE_CRIT,
                    critStats.critChance(), AttributeModifier.Operation.ADD_VALUE, EquipmentSlotGroup.MAINHAND);
            addDefaultAttribute(event, existing, MKAttributes.MELEE_CRIT_MULTIPLIER,
                    critStats.critMultiplier(), AttributeModifier.Operation.ADD_VALUE, EquipmentSlotGroup.MAINHAND);
        }
        if (from instanceof ShieldItem) {
            var shieldMods = event.getModifiers();
            Set<Holder<Attribute>> existing = new ObjectArraySet<>(shieldMods.size());
            for (var entry : shieldMods) {
                existing.add(entry.attribute());
            }

            var blockStats = ItemBlockStats.get(event.getItemStack());
            if (blockStats != null) {
                addDefaultAttribute(event, existing, MKAttributes.MAX_POISE,
                        blockStats.maxPoise(), AttributeModifier.Operation.ADD_VALUE, EquipmentSlotGroup.OFFHAND);
                addDefaultAttribute(event, existing, MKAttributes.BLOCK_EFFICIENCY,
                        blockStats.blockEfficiency(), AttributeModifier.Operation.ADD_VALUE, EquipmentSlotGroup.OFFHAND);
            }
        }
    }
}
