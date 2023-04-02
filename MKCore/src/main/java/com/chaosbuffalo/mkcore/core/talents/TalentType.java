package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.records.IRecordType;
import com.chaosbuffalo.mkcore.core.records.IRecordTypeHandler;
import com.chaosbuffalo.mkcore.core.talents.handlers.AbilityGrantTalentHandler;
import com.chaosbuffalo.mkcore.core.talents.handlers.AttributeTalentHandler;
import com.chaosbuffalo.mkcore.core.talents.handlers.EntitlementGrantTalentTypeHandler;
import com.chaosbuffalo.mkcore.init.CoreEntitlements;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.function.Function;

public class TalentType<T extends IRecordTypeHandler<TalentRecord>> implements IRecordType<T> {
    public static final TalentType<AttributeTalentHandler> ATTRIBUTE =
            new TalentType<>("mkcore.talent_type.attribute.name", AttributeTalentHandler::new)
                    .setDisplayName("mkcore.talent_type.tooltip_name");
    public static final TalentType<AbilityGrantTalentHandler> PASSIVE =
            new TalentType<>("mkcore.talent_type.passive.name", AbilityGrantTalentHandler::new)
                    .setDisplayName("mkcore.talent_type.tooltip_name_with_ability");
    public static final TalentType<AbilityGrantTalentHandler> ULTIMATE =
            new TalentType<>("mkcore.talent_type.ultimate.name", AbilityGrantTalentHandler::new)
                    .setDisplayName("mkcore.talent_type.tooltip_name_with_ability");
    public static final TalentType<EntitlementGrantTalentTypeHandler> BASIC_SLOT =
            new TalentType<>("mkcore.talent_type.basic_slot.name",
                    playerData -> new EntitlementGrantTalentTypeHandler(playerData, CoreEntitlements.BASIC_ABILITY_SLOT))
                    .setDisplayName("mkcore.talent_type.tooltip_name");
    public static final TalentType<EntitlementGrantTalentTypeHandler> PASSIVE_SLOT =
            new TalentType<>("mkcore.talent_type.passive_slot.name",
                    playerData -> new EntitlementGrantTalentTypeHandler(playerData, CoreEntitlements.PASSIVE_ABILITY_SLOT))
                    .setDisplayName("mkcore.talent_type.tooltip_name");
    public static final TalentType<EntitlementGrantTalentTypeHandler> ULTIMATE_SLOT =
            new TalentType<>("mkcore.talent_type.ultimate_slot.name",
                    playerData -> new EntitlementGrantTalentTypeHandler(playerData, CoreEntitlements.ULTIMATE_ABILITY_SLOT))
                    .setDisplayName("mkcore.talent_type.tooltip_name");
    public static final TalentType<EntitlementGrantTalentTypeHandler> POOL_COUNT =
            new TalentType<>("mkcore.talent_type.pool_slot.name",
                    playerData -> new EntitlementGrantTalentTypeHandler(playerData, CoreEntitlements.ABILITY_POOL_SIZE))
                    .setDisplayName("mkcore.talent_type.tooltip_name");

    private final String name;
    private String displayNameKey = "mkcore.talent_type.tooltip_name";
    private final Function<MKPlayerData, T> factory;

    private TalentType(String name, Function<MKPlayerData, T> factory) {
        this.name = name;
        this.factory = factory;
    }

    public TalentType<T> setDisplayName(String tooltipKey) {
        this.displayNameKey = tooltipKey;
        return this;
    }

    public TranslatableComponent getName() {
        return new TranslatableComponent(name);
    }

    public TranslatableComponent getDisplayName() {
        return new TranslatableComponent(displayNameKey, getName());
    }

    public T createTypeHandler(MKPlayerData playerData) {
        return factory.apply(playerData);
    }
}
