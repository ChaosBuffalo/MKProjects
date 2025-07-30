package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.init.CoreEntitlementTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public class AbilityPoolEntitlement extends MKEntitlement {
    public static final MapCodec<AbilityPoolEntitlement> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ComponentSerialization.CODEC.fieldOf("display_name").forGetter(MKEntitlement::getName),
            ComponentSerialization.CODEC.fieldOf("description").forGetter(MKEntitlement::getDescription),
            Codec.intRange(0, GameConstants.MAX_ABILITY_POOL_SIZE - GameConstants.DEFAULT_ABILITY_POOL_SIZE).fieldOf("max_extra_pool").forGetter(MKEntitlement::getMaxEntitlements)
    ).apply(builder, AbilityPoolEntitlement::new));


    public AbilityPoolEntitlement(Component displayName, Component description, int maxEntitlements) {
        super(displayName, description, maxEntitlements);
    }

    @Override
    public EntitlementType<AbilityPoolEntitlement> getEntitlementType() {
        return CoreEntitlementTypes.ABILITY_POOL_COUNT.get();
    }

    public static class AbilityPoolEntitlementHandler extends EntitlementTypeHandler {
        private final Persona persona;
        private final Object2IntMap<AbilityPoolEntitlement> levelsByEntitlement = new Object2IntArrayMap<>();

        public AbilityPoolEntitlementHandler(Persona persona) {
            this.persona = persona;
        }

        private void applyEffects() {
            int totalSlots = levelsByEntitlement.values().intStream().sum();
            persona.getAbilities().setAbilityPoolAddedSlots(totalSlots);
        }

        private void updateRecord(EntitlementInstance record, boolean apply) {
            if (record.entitlement().value() instanceof AbilityPoolEntitlement slotEntitlement) {
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
        public void onRecordLoadingFinished() {
            applyEffects();
        }
    }
}
