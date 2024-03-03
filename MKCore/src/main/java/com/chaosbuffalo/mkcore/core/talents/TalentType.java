package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.records.IRecordType;
import com.chaosbuffalo.mkcore.core.talents.handlers.AbilityGrantTalentHandler;
import com.chaosbuffalo.mkcore.core.talents.handlers.AttributeTalentHandler;
import com.chaosbuffalo.mkcore.core.talents.handlers.EntitlementGrantTalentTypeHandler;
import com.chaosbuffalo.mkcore.init.CoreEntitlements;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Function;

public class TalentType implements IRecordType<TalentRecord> {
    public static final TalentType ATTRIBUTE =
            new TalentType("mkcore.talent_type.attribute.name", AttributeTalentHandler::new)
                    .setDisplayName("mkcore.talent_type.tooltip_name");
    public static final TalentType PASSIVE =
            new TalentType("mkcore.talent_type.passive.name", AbilityGrantTalentHandler::new)
                    .setDisplayName("mkcore.talent_type.tooltip_name_with_ability");
    public static final TalentType ULTIMATE =
            new TalentType("mkcore.talent_type.ultimate.name", AbilityGrantTalentHandler::new)
                    .setDisplayName("mkcore.talent_type.tooltip_name_with_ability");
    public static final TalentType BASIC_SLOT =
            new TalentType("mkcore.talent_type.basic_slot.name",
                    playerData -> new EntitlementGrantTalentTypeHandler(playerData, CoreEntitlements.BASIC_ABILITY_SLOT))
                    .setDisplayName("mkcore.talent_type.tooltip_name");
    public static final TalentType PASSIVE_SLOT =
            new TalentType("mkcore.talent_type.passive_slot.name",
                    playerData -> new EntitlementGrantTalentTypeHandler(playerData, CoreEntitlements.PASSIVE_ABILITY_SLOT))
                    .setDisplayName("mkcore.talent_type.tooltip_name");
    public static final TalentType ULTIMATE_SLOT =
            new TalentType("mkcore.talent_type.ultimate_slot.name",
                    playerData -> new EntitlementGrantTalentTypeHandler(playerData, CoreEntitlements.ULTIMATE_ABILITY_SLOT))
                    .setDisplayName("mkcore.talent_type.tooltip_name");
    public static final TalentType POOL_COUNT =
            new TalentType("mkcore.talent_type.pool_slot.name",
                    playerData -> new EntitlementGrantTalentTypeHandler(playerData, CoreEntitlements.ABILITY_POOL_SIZE))
                    .setDisplayName("mkcore.talent_type.tooltip_name");

    private final String name;
    private String displayNameKey = "mkcore.talent_type.tooltip_name";
    private final Function<MKPlayerData, TalentTypeHandler> factory;

    private TalentType(String name, Function<MKPlayerData, TalentTypeHandler> factory) {
        this.name = name;
        this.factory = factory;
    }

    public TalentType setDisplayName(String tooltipKey) {
        this.displayNameKey = tooltipKey;
        return this;
    }

    public MutableComponent getName() {
        return Component.translatable(name);
    }

    public MutableComponent getDisplayName() {
        return Component.translatable(displayNameKey, getName());
    }

    public TalentTypeHandler createTypeHandler(MKPlayerData playerData) {
        return factory.apply(playerData);
    }
}
