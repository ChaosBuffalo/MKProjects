package com.chaosbuffalo.mkcore.core.talents.handlers;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.core.player.PlayerEvents;
import com.chaosbuffalo.mkcore.core.talents.TalentRecord;
import com.chaosbuffalo.mkcore.core.talents.TalentTypeHandler;
import com.chaosbuffalo.mkcore.core.talents.nodes.AttributeTalentNode;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AttributeTalentHandler extends TalentTypeHandler {
    private static final UUID EV_ID = UUID.fromString("e542745d-aa57-4093-b734-3df4deb101ff");

    protected final MKPlayerData playerData;
    private final Map<AttributeTalentNode, AttributeModifier> modifierMap = new HashMap<>();

    public AttributeTalentHandler(Persona persona) {
        super(persona);
        playerData = persona.getPlayerData();
        persona.subscribe(PlayerEvents.PERSONA_ACTIVATE, EV_ID, this::onPersonaActivated);
        persona.subscribe(PlayerEvents.PERSONA_DEACTIVATE, EV_ID, this::onPersonaDeactivated);
    }

    private void onPersonaActivated(PlayerEvents.PersonaEvent event) {
        applyAllAttributeModifiers();
    }

    private void onPersonaDeactivated(PlayerEvents.PersonaEvent event) {
        removeAllAttributeModifiers();
    }

    @Override
    public void onRecordUpdated(TalentRecord record) {
        updateTalentRecord(record, true);
    }

    @Override
    public void onRecordLoaded(TalentRecord record) {
        updateTalentRecord(record, false);
    }

    private void updateTalentRecord(TalentRecord record, boolean applyImmediately) {
        if (record.getNode() instanceof AttributeTalentNode node) {

            if (record.isKnown()) {
                AttributeModifier modifier = node.createModifier(record);
                modifierMap.put(node, modifier);
                if (applyImmediately) {
                    applyAttribute(node.getAttribute(), modifier);
                }
            } else {
                AttributeModifier existingModifier = modifierMap.remove(node);
                if (existingModifier != null) {
                    removeAttribute(node.getAttribute(), existingModifier.id());
                }
            }
        }
    }

    private void applyAttribute(Holder<Attribute> attr, AttributeModifier modifier) {
        AttributeInstance instance = playerData.getEntity().getAttribute(attr);
        if (instance == null) {
            MKCore.LOGGER.error("PlayerTalentModule.applyAttribute player did not have attribute {}!", attr);
            return;
        }

        instance.addOrUpdateTransientModifier(modifier);
    }

    private void removeAttribute(Holder<Attribute> attr, ResourceLocation modifierId) {
        AttributeInstance instance = playerData.getEntity().getAttribute(attr);
        if (instance == null) {
            MKCore.LOGGER.error("PlayerTalentModule.removeAttribute player did not have attribute {}!", attr);
            return;
        }

        instance.removeModifier(modifierId);
    }

    private void removeAllAttributeModifiers() {
        modifierMap.forEach((n, m) -> removeAttribute(n.getAttribute(), m.id()));
        modifierMap.clear();
    }

    private void applyAllAttributeModifiers() {
        modifierMap.forEach((n, m) -> applyAttribute(n.getAttribute(), m));
    }
}