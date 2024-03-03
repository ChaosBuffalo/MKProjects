package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import com.chaosbuffalo.mkcore.utils.ItemUtils;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
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

    private static final UUID UNARMED_SKILL_MODIFIER = UUID.fromString("bfd1de0f-440c-4029-bcbd-eb25dd89ee83");

    private static final float UNARMED_BASE_DAMAGE = 2.0f;

    private static final UUID CRIT_CHANCE_MODIFIER = UUID.fromString("3935094f-87c5-49a8-bcde-ea29ce3bb5f9");
    private static final UUID CRIT_MULT_MODIFIER = UUID.fromString("c167f8f7-7bfc-4232-a321-ba635a4eb46f");


    public static void removeUnarmedModifier(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attr != null) {
            attr.removeModifier(UNARMED_SKILL_MODIFIER);
        }
    }

    public static void addUnarmedModifier(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attr != null) {
            if (attr.getModifier(UNARMED_SKILL_MODIFIER) == null) {
                float skillLevel = MKAbility.getSkillLevel(entity, MKAttributes.HAND_TO_HAND);
                attr.addTransientModifier(new AttributeModifier(UNARMED_SKILL_MODIFIER, "skill scaling",
                        skillLevel * UNARMED_BASE_DAMAGE, AttributeModifier.Operation.ADDITION));
            }
        }
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity().getCommandSenderWorld().isClientSide()) {
            return;
        }
        if (event.getEntity() instanceof Player) {
            MKCore.getPlayer(event.getEntity()).ifPresent(playerData -> {
                playerData.getEquipment().onEquipmentChange(event.getSlot(), event.getFrom(), event.getTo());
                SpellTriggers.LIVING_EQUIPMENT_CHANGE.onEquipmentChange(event, playerData);
            });
        } else {
            MKCore.getEntityData(event.getEntity()).ifPresent(entityData -> {
                SpellTriggers.LIVING_EQUIPMENT_CHANGE.onEquipmentChange(event, entityData);
            });
        }
        if (event.getFrom() == ItemStack.EMPTY) {
            removeUnarmedModifier(event.getEntity());
        }
        if (event.getTo() == ItemStack.EMPTY) {
            addUnarmedModifier(event.getEntity());
        }
    }

    private static AttributeModifier createDefaultSlotModifier(UUID uuid, double amount, AttributeModifier.Operation mod) {
        return new AttributeModifier(uuid, () -> "MK:Core default bonus", amount, mod);
    }

    private static void addDefaultAttribute(ItemAttributeModifierEvent event, Attribute attribute, Supplier<AttributeModifier> modifierSupplier) {
        if (!event.getModifiers().containsKey(attribute)) {
            event.addModifier(attribute, modifierSupplier.get());
        }
    }

    @SubscribeEvent
    public static void onItemAttributeModifierEvent(ItemAttributeModifierEvent event) {
        Item from = event.getItemStack().getItem();
        if (event.getSlotType().getType() == EquipmentSlot.Type.HAND) {
            int handIndex = event.getSlotType().getIndex();
            if (from instanceof SwordItem && event.getSlotType() == EquipmentSlot.MAINHAND) {
                addDefaultAttribute(event, MKAttributes.MAX_POISE,
                        () -> createDefaultSlotModifier(SWORD_POISE_MOD_UUID[handIndex],
                                20.0,
                                AttributeModifier.Operation.ADDITION));

                addDefaultAttribute(event, MKAttributes.BLOCK_EFFICIENCY,
                        () -> createDefaultSlotModifier(SWORD_EFFICIENCY_MOD_UUID[handIndex],
                                0.75,
                                AttributeModifier.Operation.ADDITION));

                addDefaultAttribute(event, MKAttributes.MELEE_CRIT,
                        () -> createDefaultSlotModifier(CRIT_CHANCE_MODIFIER,
                                ItemUtils.getCritChanceForItem(event.getItemStack()),
                                AttributeModifier.Operation.ADDITION));

                addDefaultAttribute(event, MKAttributes.MELEE_CRIT_MULTIPLIER,
                        () -> createDefaultSlotModifier(CRIT_MULT_MODIFIER,
                                ItemUtils.getCritMultiplierForItem(event.getItemStack()),
                                AttributeModifier.Operation.ADDITION));
            }
            if (from instanceof ShieldItem && (event.getSlotType() == EquipmentSlot.OFFHAND || event.getSlotType() == EquipmentSlot.MAINHAND)) {
                addDefaultAttribute(event, MKAttributes.MAX_POISE,
                        () -> createDefaultSlotModifier(SHIELD_POISE_MOD_UUID[handIndex],
                                50.0,
                                AttributeModifier.Operation.ADDITION));
                addDefaultAttribute(event, MKAttributes.BLOCK_EFFICIENCY,
                        () -> createDefaultSlotModifier(SHIELD_EFFICIENCY_MOD_UUID[handIndex],
                                1.0f,
                                AttributeModifier.Operation.ADDITION));
            }
        }
    }
}
