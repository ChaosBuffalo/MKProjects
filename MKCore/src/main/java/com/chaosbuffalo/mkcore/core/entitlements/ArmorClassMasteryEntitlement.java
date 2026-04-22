package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.init.CoreEntitlementTypes;
import com.chaosbuffalo.mkcore.item.ArmorClass;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
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

    public static class ArmorClassMasteryHandler implements EntitlementTypeHandler {
        private final Persona persona;
        private final Object2IntMap<ArmorClassMasteryEntitlement> levelsByEntitlement = new Object2IntArrayMap<>();

        public ArmorClassMasteryHandler(Persona persona) {
            this.persona = persona;
        }

        private void applyEffects() {
            Object2IntArrayMap<ResourceKey<ArmorClass>> totals = new Object2IntArrayMap<>(levelsByEntitlement.size());
            for (var entry : levelsByEntitlement.object2IntEntrySet()) {
                totals.mergeInt(entry.getKey().armorClassKey, entry.getIntValue(), Integer::sum);
            }
            totals.object2IntEntrySet().forEach(e ->
                    persona.getPlayerData().getEquipment().enableArmorMastery(e.getKey(), e.getIntValue() > 0));
        }

        private void updateRecord(EntitlementInstance record, boolean apply) {
            if (record.entitlement().value() instanceof ArmorClassMasteryEntitlement slotEntitlement) {
                int count = persona.getEntitlements().getEntitlementLevel(slotEntitlement);
                levelsByEntitlement.put(slotEntitlement, count);
                if (apply) {
                    applyEffects();
                }
            }
        }

        @Override
        public void onRecordUpdated(EntitlementInstance record) {
            updateRecord(record, true);
        }

        @Override
        public void onRecordLoaded(EntitlementInstance record) {
            updateRecord(record, false);
        }

        @Override
        public void onPersonaActivated() {
            applyEffects();
        }
    }
}
