package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;
import com.chaosbuffalo.mkcore.init.CoreEntitlementTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public class AbilitySlotEntitlement extends MKEntitlement {
    public static final MapCodec<AbilitySlotEntitlement> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ComponentSerialization.CODEC.fieldOf("display_name").forGetter(MKEntitlement::getName),
            ComponentSerialization.CODEC.fieldOf("description").forGetter(MKEntitlement::getDescription),
            AbilityGroupId.CODEC.fieldOf("ability_group").forGetter(AbilitySlotEntitlement::getGroup)
    ).apply(builder, AbilitySlotEntitlement::new));

    private final AbilityGroupId group;

    public AbilitySlotEntitlement(Component displayName, Component description, AbilityGroupId group) {
        super(displayName, description, group.getMaxSlots());
        this.group = group;
    }

    public AbilityGroupId getGroup() {
        return group;
    }

    @Override
    public EntitlementType<AbilitySlotEntitlement> getEntitlementType() {
        return CoreEntitlementTypes.ABILITY_SLOT.get();
    }

    public static class AbilitySlotEntitlementHandler implements EntitlementTypeHandler {
        private final Persona persona;
        private final Object2IntMap<AbilitySlotEntitlement> levelsByEntitlement = new Object2IntArrayMap<>(AbilityGroupId.VALUES.length);

        public AbilitySlotEntitlementHandler(Persona persona) {
            this.persona = persona;
        }

        private void applyEffects() {
            Object2IntArrayMap<AbilityGroupId> totals = new Object2IntArrayMap<>(AbilityGroupId.VALUES.length);
            for (var entry : levelsByEntitlement.object2IntEntrySet()) {
                totals.mergeInt(entry.getKey().getGroup(), entry.getIntValue(), Integer::sum);
            }
            for (AbilityGroupId groupId : AbilityGroupId.VALUES) {
                persona.getLoadout().getAbilityGroup(groupId).setBonusSlots(totals.getInt(groupId));
            }
        }

        private void updateRecord(EntitlementInstance record, boolean apply) {
            if (record.entitlement().value() instanceof AbilitySlotEntitlement slotEntitlement) {
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
