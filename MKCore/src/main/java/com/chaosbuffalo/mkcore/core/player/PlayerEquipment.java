package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.entity.EntityEquipment;
import com.chaosbuffalo.mkcore.item.ArmorClass;
import com.chaosbuffalo.mkcore.item.CoreItemComponents;
import com.chaosbuffalo.mkcore.item.ItemGrantedAbility;
import com.chaosbuffalo.mkcore.sync.types.SyncString;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.*;

public class PlayerEquipment extends EntityEquipment implements IPlayerSyncComponentProvider {
    private static final UUID EV_ID = UUID.fromString("951a29de-b941-4c4d-9d01-dba4c68b7897");

    private final MKPlayerData playerData;
    private final PlayerSyncComponent sync = new PlayerSyncComponent();
    private final Set<ResourceLocation> armorMastery;
    private final SyncString clientMasteryInfo;

    public PlayerEquipment(MKPlayerData playerData) {
        super(playerData);
        this.playerData = playerData;
        this.armorMastery = new HashSet<>();
        clientMasteryInfo = new SyncString(""); // TODO: better sync? this is pretty dumb
        clientMasteryInfo.setCallback(this::handleClientMasteryUpdate);
        addSyncPrivate("armor_mastery", clientMasteryInfo);
        playerData.events().subscribe(PlayerEvents.PERSONA_ACTIVATE, EV_ID, this::onPersonaActivated);
        playerData.events().subscribe(PlayerEvents.PERSONA_DEACTIVATE, EV_ID, this::onPersonaDeactivated);
    }

    @Override
    public PlayerSyncComponent getSyncComponent() {
        return sync;
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
            refreshArmorClassBonus(slot);
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

    private void applyArmorClassBonus(EquipmentSlot slot, ItemStack to) {
        if (to.isEmpty())
            return;

        Holder<ArmorClass> holder = ArmorClass.getHolder(to);
        if (holder == null)
            return;

        ArmorClass armorClass = holder.value();
        armorClass.getPositiveModifierMap(slot).forEach((attr, mod) -> tryAddModifier(attr, slot, mod));
        if (!isArmorClassMastered(holder)) {
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
            removeArmorClassBonus(slot, item);
            applyArmorClassBonus(slot, item);
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
            instance.addTransientModifier(mod);
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
    public void addUnarmedModifier() {
        super.addUnarmedModifier();
        AttributeInstance attr = playerData.getEntity().getAttribute(MKAttributes.MELEE_CRIT);
        float skillLevel = MKAbility.getSkillLevel(playerData.getEntity(), MKAttributes.HAND_TO_HAND);
        if (attr != null) {
            if (attr.getModifier(UNARMED_SKILL_ID) == null) {
                attr.addTransientModifier(new AttributeModifier(UNARMED_SKILL_ID,
                        0.05 + skillLevel / 100.0, AttributeModifier.Operation.ADD_VALUE));
            }
        }
        AttributeInstance crit = playerData.getEntity().getAttribute(MKAttributes.MELEE_CRIT_MULTIPLIER);
        if (crit != null) {
            if (crit.getModifier(UNARMED_SKILL_ID) == null) {
                crit.addTransientModifier(new AttributeModifier(UNARMED_SKILL_ID,
                        0.5 + skillLevel / 10.0, AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }

    @Override
    public void removeUnarmedModifier() {
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

    public void onPersonaActivated(PlayerEvents.PersonaEvent event) {
        refreshAllArmorSlots();
        addItemAbility(EquipmentSlot.MAINHAND);
        addItemAbility(EquipmentSlot.HEAD);
        addItemAbility(EquipmentSlot.CHEST);
        addItemAbility(EquipmentSlot.LEGS);
        addItemAbility(EquipmentSlot.FEET);
    }

    private void onPersonaDeactivated(PlayerEvents.PersonaEvent event) {
        resetArmorMastery();
        removeItemAbility(EquipmentSlot.MAINHAND);
        removeItemAbility(EquipmentSlot.HEAD);
        removeItemAbility(EquipmentSlot.CHEST);
        removeItemAbility(EquipmentSlot.LEGS);
        removeItemAbility(EquipmentSlot.FEET);
    }
}
