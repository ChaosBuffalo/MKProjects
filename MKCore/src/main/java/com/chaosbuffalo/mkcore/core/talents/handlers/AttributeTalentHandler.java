package com.chaosbuffalo.mkcore.core.talents.handlers;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.talents.TalentRecord;
import com.chaosbuffalo.mkcore.core.talents.TalentTypeHandler;
import com.chaosbuffalo.mkcore.core.talents.nodes.AttributeTalentNode;
import com.chaosbuffalo.mkcore.core.talents.talent_types.AttributeTalent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.*;

public class AttributeTalentHandler extends TalentTypeHandler {

    private final Map<AttributeTalent, AttributeEntry> attributeEntryMap = new HashMap<>();

    public AttributeTalentHandler(MKPlayerData playerData) {
        super(playerData);
    }

    @Override
    public void onPersonaActivated() {
        applyAllAttributeModifiers();
    }

    @Override
    public void onPersonaDeactivated() {
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

            AttributeEntry entry = getAttributeEntry(node.getTalent());
            entry.updateTalent(record);
            if (applyImmediately) {
//                MKCore.LOGGER.info("AttributeTalentHandler.updateTalentRecord applying updated attribute {}", entry);
                applyAttribute(entry);
            }
        }
    }

    private void dumpAttrInstance(AttributeInstance instance) {
        MKCore.LOGGER.info("\tAttribute {}", instance.getAttribute().getDescriptionId());
        for (AttributeModifier modifier : instance.getModifiers()) {
            MKCore.LOGGER.info("\t\tmodifier {}", modifier);
        }
    }

    private void dumpAttributes(String location) {
        MKCore.LOGGER.info("All Attributes @ {}", location);
        AttributeMap map = playerData.getEntity().getAttributes();

//        map.getAllAttributes().forEach(this::dumpAttrInstance);
    }

    private void dumpDirtyAttributes(String location) {
        AttributeMap map = playerData.getEntity().getAttributes();

        MKCore.LOGGER.info("Dirty Attributes @ {}", location);
        map.getDirtyAttributes().forEach(this::dumpAttrInstance);
    }

    private void applyAttribute(AttributeEntry entry) {
        AttributeInstance instance = playerData.getEntity().getAttribute(entry.getAttribute());
        if (instance == null) {
            MKCore.LOGGER.error("PlayerTalentModule.applyAttribute player did not have attribute {}!", entry.getAttribute());
            return;
        }

        AttributeModifier mod = entry.getModifier();
        instance.removeModifier(mod);
        instance.addTransientModifier(mod);
        if (entry.getAttributeTalent().requiresStatRefresh()) {
            playerData.getStats().refreshStats();
        }

//        dumpDirtyAttributes("applyAttribute");
    }

    private void removeAttribute(AttributeTalent attributeTalent) {
        AttributeEntry entry = attributeEntryMap.get(attributeTalent);
        if (entry != null) {
            AttributeInstance instance = playerData.getEntity().getAttribute(entry.getAttribute());
            if (instance != null) {
                instance.removeModifier(entry.getModifier());
//                dumpDirtyAttributes("removeAttribute");
            }
        }
    }

    private void removeAllAttributeModifiers() {
        attributeEntryMap.forEach((talent, entry) -> removeAttribute(talent));
        attributeEntryMap.clear();

//        dumpAttributes("clearAttributeModifiers");
//        dumpDirtyAttributes("clearAttributeModifiers");
    }


    private void applyAllAttributeModifiers() {
        attributeEntryMap.forEach((talent, entry) -> applyAttribute(entry));
    }

    private AttributeTalentHandler.AttributeEntry getAttributeEntry(AttributeTalent attribute) {
        return attributeEntryMap.computeIfAbsent(attribute, AttributeEntry::new);
    }

    private static class AttributeEntry {
        private final AttributeTalent attribute;
        private final Set<TalentRecord> records = new HashSet<>();
        private AttributeModifier modifier;
        private double value;
        private boolean dirty = true;

        public AttributeEntry(AttributeTalent attribute) {
            this.attribute = attribute;
        }

        public AttributeTalent getAttributeTalent() {
            return attribute;
        }

        public Attribute getAttribute() {
            return attribute.getAttribute();
        }

        public Collection<TalentRecord> getRecords() {
            return records;
        }

        public AttributeModifier getModifier() {
            double rank = getTotalValue();
            if (modifier == null || modifier.getAmount() != rank) {
                modifier = attribute.createModifier(rank);
            }
            return modifier;
        }

        public double getTotalValue() {
            if (dirty) {
                value = calculateValue();
                dirty = false;
            }
            return value;
        }

        private double calculateValue() {
            return records.stream().mapToDouble(r -> ((AttributeTalentNode) r.getNode()).getValue(r.getRank())).sum();
        }

        public void updateTalent(TalentRecord record) {
            boolean changed = records.add(record);
            dirty = true;
        }

        @Override
        public String toString() {
            return "AttributeEntry{" +
                    "attribute=" + attribute +
                    ", value=" + getModifier().getAmount() +
                    ", dirty=" + dirty +
                    ", modifier=" + getModifier() +
                    '}';
        }
    }
}