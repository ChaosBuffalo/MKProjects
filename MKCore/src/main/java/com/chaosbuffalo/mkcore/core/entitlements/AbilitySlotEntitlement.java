package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;
import com.chaosbuffalo.mkcore.init.CoreEntitlementTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public class AbilitySlotEntitlement extends MKEntitlement {
    public static final MapCodec<AbilitySlotEntitlement> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ComponentSerialization.CODEC.fieldOf("display_name").forGetter(MKEntitlement::getName),
            ComponentSerialization.CODEC.fieldOf("description").forGetter(MKEntitlement::getDescription),
            AbilityGroupId.CODEC.fieldOf("ability_group").forGetter(i -> i.group)
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

    public static class AbilitySlotEntitlementHandler extends EntitlementTypeHandler {
        private final Persona persona;

        public AbilitySlotEntitlementHandler(Persona persona) {
            this.persona = persona;
        }

        private void recalculateSlots(AbilitySlotEntitlement entitlement) {
            int count = persona.getEntitlements().getEntitlementLevel(entitlement);
            persona.getLoadout()
                    .getAbilityGroup(entitlement.getGroup())
                    .setBonusSlots(count);
        }

        @Override
        public void onRecordUpdated(EntitlementInstance record) {
            if (record.entitlement().value() instanceof AbilitySlotEntitlement slotEntitlement) {
                recalculateSlots(slotEntitlement);
            }
        }

        @Override
        public void onRecordLoaded(EntitlementInstance record) {
            onRecordUpdated(record);
        }
    }
}
