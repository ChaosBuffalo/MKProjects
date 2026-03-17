package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.entity.EntityEquipment;
import com.chaosbuffalo.mkcore.events.PersonaEvent;
import com.chaosbuffalo.mkcore.item.ArmorClass;
import com.chaosbuffalo.mkcore.item.CoreItemComponents;
import com.chaosbuffalo.mkcore.item.ItemGrantedAbility;
import com.chaosbuffalo.mkcore.sync.types.SyncString;
import com.chaosbuffalo.mkcore.sync.v2.ISyncGroupProvider;
import com.chaosbuffalo.mkcore.sync.v2.SyncGroup;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import javax.annotation.Nonnull;
import java.util.*;

public class PlayerEquipment extends EntityEquipment implements ISyncGroupProvider {

    private final MKPlayerData playerData;
    private final SyncGroup syncGroup = new SyncGroup();
    private final Set<ResourceLocation> armorMastery;
    private final SyncString clientMasteryInfo;
    private boolean lastHandsEmpty = false;

    public PlayerEquipment(MKPlayerData playerData) {
        super(playerData);
        this.playerData = playerData;
        this.armorMastery = new HashSet<>();
        clientMasteryInfo = new SyncString(""); // TODO: better sync? this is pretty dumb
        clientMasteryInfo.setCallback(this::handleClientMasteryUpdate);
        syncGroup.addPrivate("armor_mastery", clientMasteryInfo);
    }

    @Override
    public SyncGroup getSyncGroup() {
        return syncGroup;
    }

    private void handleClientMasteryUpdate(String masteryInfo) {
        armorMastery.clear();
        Arrays.stream(masteryInfo.split("\\|")).map(ResourceLocation::parse).forEach(armorMastery::add);
    }

    public void enableArmorMastery(ResourceKey<ArmorClass> armorClassResourceKey, boolean enable) {
        if (enable) {
            armorMastery.add(armorClassResourceKey.location());
        } else {
            armorMastery.remove(armorClassResourceKey.location());
        }
        // Inform the client about known mastery so tooltips work properly
        updateClientMastery();
        refreshAllArmorSlots();
    }

    @Override
    protected void handleEquip(EquipmentSlot slot, ItemStack to) {
        super.handleEquip(slot, to);
        addItemAbility(slot, to);
        if (slot.isArmor()) {
            // Need to do refresh here so armor mastery is applied to the armor worn at login
            // persona activate callback is done before equipment is ready
            refreshArmorClassBonus(slot, to);
        }
    }

    @Override
    protected void handleRemoval(EquipmentSlot slot, ItemStack from) {
        super.handleRemoval(slot, from);
        removeItemAbility(slot, from);
        if (slot.isArmor()) {
            removeArmorClassBonus(slot, from);
        }
    }

    private void refreshAllArmorSlots() {
        // For all armor, reapply effects to account for new mastery
        refreshArmorClassBonus(EquipmentSlot.HEAD);
        refreshArmorClassBonus(EquipmentSlot.CHEST);
        refreshArmorClassBonus(EquipmentSlot.LEGS);
        refreshArmorClassBonus(EquipmentSlot.FEET);
    }

    private void updateClientMastery() {
        // Inform the client about known mastery so tooltips work properly
        clientMasteryInfo.set(String.join("|", armorMastery.stream().map(ResourceLocation::toString).toList()));
    }

    private void resetArmorMastery() {
        armorMastery.clear();
        updateClientMastery();
        refreshAllArmorSlots();
    }

    public boolean isArmorClassMastered(@Nonnull Holder<ArmorClass> armorClassHolder) {
        ResourceKey<ArmorClass> armorKey = armorClassHolder.getKey();
        return armorKey != null && armorMastery.contains(armorKey.location());
    }

    private void refreshArmorClassBonus(EquipmentSlot slot, ItemStack to) {
        if (to.isEmpty())
            return;

        Holder<ArmorClass> holder = ArmorClass.getHolder(to);
        if (holder == null)
            return;

        ArmorClass armorClass = holder.value();
        armorClass.getPositiveModifierMap(slot).forEach((attr, mod) -> tryAddModifier(attr, slot, mod));
        if (isArmorClassMastered(holder)) {
            armorClass.getNegativeModifierMap(slot).forEach((attr, mod) -> tryRemoveModifier(attr, slot, mod));
        } else {
            armorClass.getNegativeModifierMap(slot).forEach((attr, mod) -> tryAddModifier(attr, slot, mod));
        }
    }

    private void removeArmorClassBonus(EquipmentSlot slot, ItemStack from) {
        if (from.isEmpty())
            return;

        Holder<ArmorClass> holder = ArmorClass.getHolder(from);
        if (holder == null)
            return;

        ArmorClass armorClass = holder.value();
        armorClass.getPositiveModifierMap(slot).forEach((attr, mod) -> tryRemoveModifier(attr, slot, mod));
        armorClass.getNegativeModifierMap(slot).forEach((attr, mod) -> tryRemoveModifier(attr, slot, mod));
    }

    private void refreshArmorClassBonus(EquipmentSlot slot) {
        var item = playerData.getEntity().getItemBySlot(slot);
        if (!item.isEmpty()) {
            refreshArmorClassBonus(slot, item);
        }
    }

    private ResourceLocation makeSlotModifierId(AttributeModifier template, EquipmentSlot slot) {
        return template.id().withSuffix("." + slot.getName());
    }

    private void tryAddModifier(Holder<Attribute> attribute, EquipmentSlot slot, AttributeModifier template) {
        AttributeInstance instance = getEntityData().getEntity().getAttribute(attribute);
        if (instance != null) {
            var modId = makeSlotModifierId(template, slot);
            AttributeModifier mod = new AttributeModifier(modId, template.amount(), template.operation());
            instance.addOrUpdateTransientModifier(mod);
        }
    }

    private void tryRemoveModifier(Holder<Attribute> attribute, EquipmentSlot slot, AttributeModifier template) {
        AttributeInstance instance = getEntityData().getEntity().getAttribute(attribute);
        if (instance != null) {
            var modId = makeSlotModifierId(template, slot);
            instance.removeModifier(modId);
        }
    }

    private void addItemAbility(EquipmentSlot slot) {
        ItemStack newItem = playerData.getEntity().getItemBySlot(slot);
        addItemAbility(slot, newItem);
    }

    private void addItemAbility(EquipmentSlot slot, ItemStack newItem) {
        if (newItem.isEmpty())
            return;

        ItemGrantedAbility itemAbility = newItem.get(CoreItemComponents.ITEM_ABILITY);
        if (itemAbility != null) {
            MKAbility ability = itemAbility.ability().value();
            playerData.getLoadout().getItemGroup().setSlot(slot, ability);
        } else {
            playerData.getLoadout().getItemGroup().clearSlot(slot);
        }
    }

    private void removeItemAbility(EquipmentSlot slot) {
        ItemStack oldItem = playerData.getEntity().getItemBySlot(slot);
        removeItemAbility(slot, oldItem);
    }

    private void removeItemAbility(EquipmentSlot slot, ItemStack oldItem) {
        if (oldItem.isEmpty())
            return;

        ItemGrantedAbility itemAbility = oldItem.get(CoreItemComponents.ITEM_ABILITY);
        if (itemAbility != null) {
            var existingAbilityId = playerData.getLoadout().getItemGroup().getSlot(slot);
            if (!existingAbilityId.equals(MKCoreRegistry.INVALID_ABILITY) && !itemAbility.ability().is(existingAbilityId)) {
                MKCore.LOGGER.warn("Player {} unequipping slot {} had differing item abilities! Found {}, expected {} on {}", playerData.getEntity(), slot, itemAbility.ability().value(), existingAbilityId, oldItem);
            }
            playerData.getLoadout().getItemGroup().clearSlot(slot);
        }
    }

    @Override
    protected void addUnarmedModifier() {
        super.addUnarmedModifier();
        float skillLevel = MKAbility.getSkillLevel(playerData.getEntity(), MKAttributes.HAND_TO_HAND);

        AttributeInstance attr = playerData.getEntity().getAttribute(MKAttributes.MELEE_CRIT);
        if (attr != null) {
            double amount = 0.05 + skillLevel / 100.0;
            attr.addOrUpdateTransientModifier(new AttributeModifier(UNARMED_SKILL_ID,
                    amount, AttributeModifier.Operation.ADD_VALUE));
        }

        AttributeInstance crit = playerData.getEntity().getAttribute(MKAttributes.MELEE_CRIT_MULTIPLIER);
        if (crit != null) {
            double amount = 0.5 + skillLevel / 10.0;
            crit.addOrUpdateTransientModifier(new AttributeModifier(UNARMED_SKILL_ID,
                    amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    @Override
    protected void removeUnarmedModifier() {
        super.removeUnarmedModifier();
        AttributeInstance attr = playerData.getEntity().getAttribute(MKAttributes.MELEE_CRIT);
        if (attr != null) {
            attr.removeModifier(UNARMED_SKILL_ID);
        }
        AttributeInstance crit = playerData.getEntity().getAttribute(MKAttributes.MELEE_CRIT_MULTIPLIER);
        if (crit != null) {
            crit.removeModifier(UNARMED_SKILL_ID);
        }
    }

    @Override
    public void refreshUnarmedModifiers(ItemStack mainHand) {
        refreshUnarmedModifiers(mainHand, false);
    }

    private void refreshUnarmedModifiers(ItemStack mainHand, boolean force) {
        if (force || lastHandsEmpty != mainHand.isEmpty()) {
            super.refreshUnarmedModifiers(mainHand);
            lastHandsEmpty = mainHand.isEmpty();
        }
    }

    private void onPersonaActivated() {
        refreshAllArmorSlots();
        addItemAbility(EquipmentSlot.MAINHAND);
        addItemAbility(EquipmentSlot.HEAD);
        addItemAbility(EquipmentSlot.CHEST);
        addItemAbility(EquipmentSlot.LEGS);
        addItemAbility(EquipmentSlot.FEET);

        ItemStack mainHand = playerData.getEntity().getItemBySlot(EquipmentSlot.MAINHAND);
        refreshUnarmedModifiers(mainHand, true);
    }

    private void onPersonaDeactivated() {
        resetArmorMastery();
        removeItemAbility(EquipmentSlot.MAINHAND);
        removeItemAbility(EquipmentSlot.HEAD);
        removeItemAbility(EquipmentSlot.CHEST);
        removeItemAbility(EquipmentSlot.LEGS);
        removeItemAbility(EquipmentSlot.FEET);
    }

    @EventBusSubscriber
    public static class Events {

        @SubscribeEvent
        public static void onPersonaActivated(PersonaEvent.PersonaActivated event) {
            event.getPlayerData().getEquipment().onPersonaActivated();
        }

        @SubscribeEvent
        public static void onPersonaDeactivated(PersonaEvent.PersonaDeactivated event) {
            event.getPlayerData().getEquipment().onPersonaDeactivated();
        }
    }
}
