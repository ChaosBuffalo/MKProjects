package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKAbilityProvider;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.entity.EntityEquipment;
import com.chaosbuffalo.mkcore.item.ArmorClass;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class PlayerEquipment extends EntityEquipment {
    private static final UUID[] ARMOR_CLASS_UUID_BY_SLOT = new UUID[]{
            UUID.fromString("536049db-3699-4cff-831c-52fe99b24269"),
            UUID.fromString("75a8a55f-13de-400f-a823-444e71729fd5"),
            UUID.fromString("c787ae8b-6cc1-4b72-ac00-e047f5005c32"),
            UUID.fromString("d598564a-84be-46fe-ac46-3028c6e45dd1"),
            UUID.fromString("38e5df08-9bd6-446e-a75d-f0b2aa150a73"),
            UUID.fromString("9b444ef7-5020-483e-b355-7b975958634a")
    };

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
            addItemAbility(to);
        } else if (slot == EquipmentSlot.MAINHAND) {
            handleMainHandChange(to);
        }
    }

    @Override
    protected void handleRemoval(EquipmentSlot slot, ItemStack from) {
        super.handleRemoval(slot, from);
        if (slot.isArmor() && !from.isEmpty()) {
            removeArmorClassBonus(slot, from);
            removeItemAbility(from);
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

        if (to.getItem() instanceof IMKAbilityProvider provider) {
            currentMainAbility = provider.getAbility(to);
            if (currentMainAbility != null) {
                playerData.getLoadout().getAbilityGroup(AbilityGroupId.Item).setSlot(0, currentMainAbility.getAbilityId());
            }
        }
    }

    private UUID getArmorClassSlotUUID(EquipmentSlot slot) {
        return ARMOR_CLASS_UUID_BY_SLOT[slot.ordinal()];
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
            UUID uuid = getArmorClassSlotUUID(slot);
            itemClass.getPositiveModifierMap(slot).keySet().forEach(attr -> tryRemoveModifier(attr, uuid));
            itemClass.getNegativeModifierMap(slot).keySet().forEach(attr -> tryRemoveModifier(attr, uuid));
        }
    }

    private void tryAddModifier(Attribute attribute, EquipmentSlot slot, AttributeModifier template) {
        AttributeInstance instance = getEntityData().getEntity().getAttribute(attribute);
        if (instance != null) {
            UUID uuid = getArmorClassSlotUUID(slot);
            AttributeModifier mod = new AttributeModifier(uuid, template::getName, template.getAmount(), template.getOperation());
            instance.addTransientModifier(mod);
        }
    }

    private void tryRemoveModifier(Attribute attr, UUID uuid) {
        AttributeInstance instance = getEntityData().getEntity().getAttribute(attr);
        if (instance != null) {
            instance.removeModifier(uuid);
        }
    }

    private void addItemAbility(ItemStack newItem) {
        if (newItem.isEmpty())
            return;

        if (newItem.getItem() instanceof IMKAbilityProvider provider) {
            MKAbility ability = provider.getAbility(newItem);
            if (ability != null) {
                EquipmentSlot slot = LivingEntity.getEquipmentSlotForItem(newItem);
                playerData.getAbilities().learnAbility(ability, AbilitySource.forEquipmentSlot(slot));
            }
        }
    }

    private void removeItemAbility(ItemStack oldItem) {
        if (oldItem.isEmpty())
            return;

        if (oldItem.getItem() instanceof IMKAbilityProvider provider) {
            MKAbility ability = provider.getAbility(oldItem);
            if (ability != null) {
                EquipmentSlot slot = LivingEntity.getEquipmentSlotForItem(oldItem);
                playerData.getAbilities().unlearnAbility(ability.getAbilityId(), AbilitySource.forEquipmentSlot(slot));
            }
        }
    }

    public void onPersonaActivated(PlayerEvents.PersonaEvent event) {
        Player player = playerData.getEntity();
        ItemStack mainHand = player.getItemBySlot(EquipmentSlot.MAINHAND);
        handleMainHandChange(mainHand);
        addItemAbility(player.getItemBySlot(EquipmentSlot.HEAD));
        addItemAbility(player.getItemBySlot(EquipmentSlot.CHEST));
        addItemAbility(player.getItemBySlot(EquipmentSlot.LEGS));
        addItemAbility(player.getItemBySlot(EquipmentSlot.FEET));
    }

    private void onPersonaDeactivated(PlayerEvents.PersonaEvent event) {
        Player player = playerData.getEntity();
        handleMainHandChange(ItemStack.EMPTY);
        removeItemAbility(player.getItemBySlot(EquipmentSlot.HEAD));
        removeItemAbility(player.getItemBySlot(EquipmentSlot.CHEST));
        removeItemAbility(player.getItemBySlot(EquipmentSlot.LEGS));
        removeItemAbility(player.getItemBySlot(EquipmentSlot.FEET));
    }
}
