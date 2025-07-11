package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.item.ArmorClass;
import net.minecraft.resources.ResourceKey;

public class ArmorClassMasteryEntitlement extends MKEntitlement {

    public static final EntitlementType TYPE = new EntitlementType() {
        @Override
        public EntitlementTypeHandler createTypeHandler(Persona persona) {
            return new ArmorClassMasteryHandler(persona);
        }
    };

    private final ResourceKey<ArmorClass> armorClassKey;

    public ArmorClassMasteryEntitlement(ResourceKey<ArmorClass> armorClassKey) {
        super(1);
        this.armorClassKey = armorClassKey;
    }

    @Override
    public EntitlementType getEntitlementType() {
        return TYPE;
    }

    public static class ArmorClassMasteryHandler extends EntitlementTypeHandler {
        private final Persona persona;

        public ArmorClassMasteryHandler(Persona persona) {
            this.persona = persona;
        }

        private void updateMastery(ArmorClassMasteryEntitlement entitlement) {
            int count = persona.getEntitlements().getEntitlementLevel(entitlement);
            persona.getPlayerData().getEquipment().enableArmorMastery(entitlement.armorClassKey, count > 0);
        }

        @Override
        public void onRecordUpdated(EntitlementInstance record) {
            if (record.entitlement() instanceof ArmorClassMasteryEntitlement slotEntitlement) {
                updateMastery(slotEntitlement);
            }
        }

        @Override
        public void onRecordLoaded(EntitlementInstance record) {
            onRecordUpdated(record);
        }
    }
}
