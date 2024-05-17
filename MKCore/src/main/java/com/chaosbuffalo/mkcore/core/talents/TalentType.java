package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.core.persona.Persona;
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
            new TalentType("mkcore.talent_type.attribute.name", AttributeTalentHandler::new);
    public static final TalentType PASSIVE =
            new TalentType("mkcore.talent_type.passive.name", AbilityGrantTalentHandler::new)
                    .setDisplayName("mkcore.talent_type.tooltip_name_with_ability");
    public static final TalentType ULTIMATE =
            new TalentType("mkcore.talent_type.ultimate.name", AbilityGrantTalentHandler::new)
                    .setDisplayName("mkcore.talent_type.tooltip_name_with_ability");
    public static final TalentType BASIC_SLOT =
            new TalentType("mkcore.talent_type.basic_slot.name",
                    persona -> new EntitlementGrantTalentTypeHandler(persona, CoreEntitlements.BASIC_ABILITY_SLOT));
    public static final TalentType PASSIVE_SLOT =
            new TalentType("mkcore.talent_type.passive_slot.name",
                    persona -> new EntitlementGrantTalentTypeHandler(persona, CoreEntitlements.PASSIVE_ABILITY_SLOT));
    public static final TalentType ULTIMATE_SLOT =
            new TalentType("mkcore.talent_type.ultimate_slot.name",
                    persona -> new EntitlementGrantTalentTypeHandler(persona, CoreEntitlements.ULTIMATE_ABILITY_SLOT));
    public static final TalentType POOL_COUNT =
            new TalentType("mkcore.talent_type.pool_slot.name",
                    persona -> new EntitlementGrantTalentTypeHandler(persona, CoreEntitlements.ABILITY_POOL_SIZE));

    private final String name;
    private String displayNameKey = "mkcore.talent_type.tooltip_name";
    private final Function<Persona, TalentTypeHandler> factory;

    private TalentType(String name, Function<Persona, TalentTypeHandler> factory) {
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

    public TalentTypeHandler createTypeHandler(Persona persona) {
        return factory.apply(persona);
    }
}
