package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.entity.EntityEquipment;
import com.chaosbuffalo.mkcore.item.ArmorClass;
import com.chaosbuffalo.mkcore.item.CoreItemComponents;
import com.chaosbuffalo.mkcore.item.ItemGrantedAbility;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class PlayerEquipment extends EntityEquipment {
    private static final UUID EV_ID = UUID.fromString("951a29de-b941-4c4d-9d01-dba4c68b7897");

    private final MKPlayerData playerData;
    private MKAbility currentMainAbility = null;

    public PlayerEquipment(MKPlayerData playerData) {
        super(playerData);
        this.playerData = playerData;
        playerData.events().subscribe(PlayerEvents.PERSONA_ACTIVATE, EV_ID, this::onPersonaActivated);
        playerData.events().subscribe(PlayerEvents.PERSONA_DEACTIVATE, EV_ID, this::onPersonaDeactivated);
    }

    @Override
    protected void handleEquip(EquipmentSlot slot, ItemStack to) {
        super.handleEquip(slot, to);
        if (slot.isArmor()) {
            applyArmorClassBonus(slot, to);
            addItemAbility(slot, to);
        } else if (slot == EquipmentSlot.MAINHAND) {
            handleMainHandChange(to);
        }
    }

    @Override
    protected void handleRemoval(EquipmentSlot slot, ItemStack from) {
        super.handleRemoval(slot, from);
        if (slot.isArmor() && !from.isEmpty()) {
            removeArmorClassBonus(slot, from);
            removeItemAbility(slot, from);
        } else if (slot == EquipmentSlot.MAINHAND) {
            clearItemAbility();
        }
    }

    private void clearItemAbility() {
        if (currentMainAbility != null) {
            playerData.getLoadout().getAbilityGroup(AbilityGroupId.Item).clearSlot(0);
            currentMainAbility = null;
        }
    }

    private void handleMainHandChange(ItemStack to) {
        // Clear the current ability if present
        clearItemAbility();

        ItemGrantedAbility itemAbility = to.get(CoreItemComponents.ITEM_ABILITY);
        if (itemAbility != null) {
            currentMainAbility = itemAbility.ability().value();
            playerData.getLoadout().getAbilityGroup(AbilityGroupId.Item).setSlot(0, currentMainAbility.getAbilityId());
        }
    }

    private void applyArmorClassBonus(EquipmentSlot slot, ItemStack to) {
        ArmorClass armorClass = ArmorClass.getItemArmorClass(to);
        if (armorClass != null) {
            armorClass.getPositiveModifierMap(slot).forEach((attr, mod) -> tryAddModifier(attr, slot, mod));
            armorClass.getNegativeModifierMap(slot).forEach((attr, mod) -> tryAddModifier(attr, slot, mod));
        }
    }

    private void removeArmorClassBonus(EquipmentSlot slot, ItemStack from) {
        ArmorClass itemClass = ArmorClass.getItemArmorClass(from);
        if (itemClass != null) {
            itemClass.getPositiveModifierMap(slot).forEach((attr, mod) -> tryRemoveModifier(attr, slot, mod));
            itemClass.getNegativeModifierMap(slot).forEach((attr, mod) -> tryRemoveModifier(attr, slot, mod));
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
            playerData.getAbilities().learnAbility(ability, AbilitySource.forEquipmentSlot(slot));
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
            MKAbility ability = itemAbility.ability().value();
            playerData.getAbilities().unlearnAbility(ability.getAbilityId(), AbilitySource.forEquipmentSlot(slot));
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
        Player player = playerData.getEntity();
        ItemStack mainHand = player.getItemBySlot(EquipmentSlot.MAINHAND);
        handleMainHandChange(mainHand);
        addItemAbility(EquipmentSlot.HEAD);
        addItemAbility(EquipmentSlot.CHEST);
        addItemAbility(EquipmentSlot.LEGS);
        addItemAbility(EquipmentSlot.FEET);
    }

    private void onPersonaDeactivated(PlayerEvents.PersonaEvent event) {
        handleMainHandChange(ItemStack.EMPTY);
        removeItemAbility(EquipmentSlot.HEAD);
        removeItemAbility(EquipmentSlot.CHEST);
        removeItemAbility(EquipmentSlot.LEGS);
        removeItemAbility(EquipmentSlot.FEET);
    }
}
