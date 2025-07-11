package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.init.CoreEntitlementTypes;
import com.chaosbuffalo.mkcore.item.ArmorClass;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceKey;

public class ArmorClassMasteryEntitlement extends MKEntitlement {
    public static final MapCodec<ArmorClassMasteryEntitlement> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ComponentSerialization.CODEC.fieldOf("display_name").forGetter(MKEntitlement::getName),
            ComponentSerialization.CODEC.fieldOf("description").forGetter(MKEntitlement::getDescription),
            ArmorClass.KEY_CODEC.fieldOf("armor_class").forGetter(i -> i.armorClassKey)
    ).apply(builder, ArmorClassMasteryEntitlement::new));

    private final ResourceKey<ArmorClass> armorClassKey;

    public ArmorClassMasteryEntitlement(Component displayName, Component description, ResourceKey<ArmorClass> armorClassKey) {
        super(displayName, description, 1);
        this.armorClassKey = armorClassKey;
    }

    @Override
    public EntitlementType<ArmorClassMasteryEntitlement> getEntitlementType() {
        return CoreEntitlementTypes.ARMOR_CLASS_MASTERY.get();
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
            if (record.entitlement().value() instanceof ArmorClassMasteryEntitlement slotEntitlement) {
                updateMastery(slotEntitlement);
            }
        }

        @Override
        public void onRecordLoaded(EntitlementInstance record) {
            onRecordUpdated(record);
        }
    }
}
