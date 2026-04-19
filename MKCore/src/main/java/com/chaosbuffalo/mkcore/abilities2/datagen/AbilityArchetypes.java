package com.chaosbuffalo.mkcore.abilities2.datagen;

import com.chaosbuffalo.mkcore.abilities2.actions.AbilityAction;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityActivationDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityDeliveryDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityReactionDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityScalar;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityTargetResolverDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.ActivationBehavior;
import com.chaosbuffalo.mkcore.abilities2.definition.ActivationKind;
import com.chaosbuffalo.mkcore.abilities2.definition.DeliveryKind;
import com.chaosbuffalo.mkcore.abilities2.definition.InterruptPolicy;
import com.chaosbuffalo.mkcore.abilities2.definition.InterruptRefundPolicy;
import com.chaosbuffalo.mkcore.init.CoreEntities;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public final class AbilityArchetypes {
    public static final String CAST_ACTIVATION_ID = "cast";
    public static final String CAST_ENTRY_POINT = "cast";
    public static final String CHANNEL_TICK_ENTRY_POINT = "tick";

    public static final String PROJECTILE_DELIVERY_ID = "projectile";
    public static final String PROJECTILE_IMPACT_ACTIVATION_ID = "impact_proc";
    public static final String PROJECTILE_IMPACT_ENTRY_POINT = "impact_proc";

    public static final String PASSIVE_SETUP_ACTIVATION_ID = "passive_setup";
    public static final String PASSIVE_SETUP_ENTRY_POINT = "passive_setup";
    public static final String PASSIVE_TEARDOWN_ACTIVATION_ID = "passive_teardown";
    public static final String PASSIVE_TEARDOWN_ENTRY_POINT = "passive_teardown";
    public static final String PROC_ACTIVATION_ID = "proc";
    public static final String PROC_ENTRY_POINT = "proc";
    public static final String PROC_REACTION_ID = "proc_reaction";

    public static final String TOGGLE_ENABLE_ACTIVATION_ID = "toggle_enable";
    public static final String TOGGLE_ENABLE_ENTRY_POINT = "toggle_enable";
    public static final String TOGGLE_DISABLE_ACTIVATION_ID = "toggle_disable";
    public static final String TOGGLE_DISABLE_ENTRY_POINT = "toggle_disable";
    public static final String AURA_PULSE_ENTRY_POINT = "aura_pulse";

    public static final InterruptPolicy STANDARD_MANUAL_INTERRUPT = new InterruptPolicy(true, 0.0f, true, 0.1, true);
    public static final InterruptPolicy INTERNAL_INTERRUPT = new InterruptPolicy(false, 0.0f, false, 0.1, true);

    private AbilityArchetypes() {
    }

    public static AbilityDefinitionBuilder singleTargetSpell(ResourceLocation id,
                                                             String name,
                                                             String description,
                                                             AbilityTargetResolverDefinition targeting) {
        return singleTargetSpell(id, name, description, AbilityDatagenKeys.SLOT_FAMILY_BASIC, targeting);
    }

    public static AbilityDefinitionBuilder singleTargetSpell(ResourceLocation id,
                                                             String name,
                                                             String description,
                                                             ResourceLocation slotFamily,
                                                             AbilityTargetResolverDefinition targeting) {
        return AbilityDefinitionBuilder.create(id, name, description, slotFamily)
                .tag(AbilityDatagenKeys.TAG_SPELL)
                .activation(CAST_ACTIVATION_ID, new AbilityActivationDefinition(
                        ActivationKind.MANUAL,
                        CAST_ENTRY_POINT,
                        targeting,
                        List.of(),
                        List.of(),
                        null,
                        0,
                        false,
                        STANDARD_MANUAL_INTERRUPT,
                        InterruptRefundPolicy.NONE,
                        new ActivationBehavior.InstantBehavior()
                ))
                .entryPoint(CAST_ENTRY_POINT, List.of());
    }

    public static AbilityDefinitionBuilder projectileSpell(ResourceLocation id,
                                                           String name,
                                                           String description,
                                                           AbilityTargetResolverDefinition targeting,
                                                           AbilityAction.ActionTarget projectileTarget,
                                                           AbilityScalar speed,
                                                           AbilityScalar inaccuracy) {
        return projectileSpell(id, name, description, AbilityDatagenKeys.SLOT_FAMILY_BASIC, targeting,
                projectileTarget, speed, inaccuracy);
    }

    public static AbilityDefinitionBuilder projectileSpell(ResourceLocation id,
                                                           String name,
                                                           String description,
                                                           ResourceLocation slotFamily,
                                                           AbilityTargetResolverDefinition targeting,
                                                           AbilityAction.ActionTarget projectileTarget,
                                                           AbilityScalar speed,
                                                           AbilityScalar inaccuracy) {
        return AbilityDefinitionBuilder.create(id, name, description, slotFamily)
                .tag(AbilityDatagenKeys.TAG_SPELL)
                .tag(AbilityDatagenKeys.TAG_PROJECTILE)
                .activation(CAST_ACTIVATION_ID, new AbilityActivationDefinition(
                        ActivationKind.MANUAL,
                        CAST_ENTRY_POINT,
                        targeting,
                        List.of(),
                        List.of(),
                        null,
                        0,
                        false,
                        STANDARD_MANUAL_INTERRUPT,
                        InterruptRefundPolicy.NONE,
                        new ActivationBehavior.InstantBehavior()
                ))
                .activation(PROJECTILE_IMPACT_ACTIVATION_ID, new AbilityActivationDefinition(
                        ActivationKind.PROC,
                        PROJECTILE_IMPACT_ENTRY_POINT,
                        AbilityDatagenKeys.TARGET_RESOLVED,
                        List.of(),
                        List.of(),
                        null,
                        0,
                        false,
                        INTERNAL_INTERRUPT,
                        InterruptRefundPolicy.NONE,
                        new ActivationBehavior.InstantBehavior()
                ))
                .entryPoint(CAST_ENTRY_POINT, List.of(new AbilityAction.SpawnProjectileAction(
                        PROJECTILE_DELIVERY_ID,
                        projectileTarget,
                        speed,
                        inaccuracy
                )))
                .entryPoint(PROJECTILE_IMPACT_ENTRY_POINT, List.of())
                .delivery(PROJECTILE_DELIVERY_ID, new AbilityDeliveryDefinition(
                        DeliveryKind.PROJECTILE,
                        CoreEntities.ABILITY_PROJECTILE_TYPE.getId(),
                        List.of(),
                        PROJECTILE_IMPACT_ACTIVATION_ID,
                        null,
                        null
                ));
    }

    public static AbilityDefinitionBuilder passiveProc(ResourceLocation id,
                                                       String name,
                                                       String description,
                                                       AbilityReactionDefinition reaction,
                                                       AbilityTargetResolverDefinition procTargeting) {
        return AbilityDefinitionBuilder.create(id, name, description, AbilityDatagenKeys.SLOT_FAMILY_PASSIVE)
                .tag(AbilityDatagenKeys.TAG_PASSIVE)
                .activation(PASSIVE_SETUP_ACTIVATION_ID, new AbilityActivationDefinition(
                        ActivationKind.PASSIVE_SETUP,
                        PASSIVE_SETUP_ENTRY_POINT,
                        AbilityDatagenKeys.TARGET_NONE,
                        List.of(),
                        List.of(),
                        null,
                        0,
                        false,
                        INTERNAL_INTERRUPT,
                        InterruptRefundPolicy.NONE,
                        new ActivationBehavior.InstantBehavior()
                ))
                .activation(PASSIVE_TEARDOWN_ACTIVATION_ID, new AbilityActivationDefinition(
                        ActivationKind.PASSIVE_TEARDOWN,
                        PASSIVE_TEARDOWN_ENTRY_POINT,
                        AbilityDatagenKeys.TARGET_NONE,
                        List.of(),
                        List.of(),
                        null,
                        0,
                        false,
                        INTERNAL_INTERRUPT,
                        InterruptRefundPolicy.NONE,
                        new ActivationBehavior.InstantBehavior()
                ))
                .activation(PROC_ACTIVATION_ID, new AbilityActivationDefinition(
                        ActivationKind.PROC,
                        PROC_ENTRY_POINT,
                        procTargeting,
                        List.of(),
                        List.of(),
                        null,
                        0,
                        false,
                        INTERNAL_INTERRUPT,
                        InterruptRefundPolicy.NONE,
                        new ActivationBehavior.InstantBehavior()
                ))
                .entryPoint(PASSIVE_SETUP_ENTRY_POINT, List.of(new AbilityAction.InstallReactionAction(PROC_REACTION_ID)))
                .entryPoint(PASSIVE_TEARDOWN_ENTRY_POINT, List.of(new AbilityAction.RemoveReactionAction(PROC_REACTION_ID)))
                .entryPoint(PROC_ENTRY_POINT, List.of())
                .reaction(PROC_REACTION_ID, reaction);
    }

    public static AbilityDefinitionBuilder channelSpell(ResourceLocation id,
                                                        String name,
                                                        String description,
                                                        AbilityTargetResolverDefinition targeting,
                                                        int tickIntervalTicks,
                                                        boolean preserveInitialTargets,
                                                        @Nullable AbilityTargetResolverDefinition tickTargeting) {
        return channelSpell(id, name, description, AbilityDatagenKeys.SLOT_FAMILY_BASIC, targeting,
                tickIntervalTicks, preserveInitialTargets, tickTargeting);
    }

    public static AbilityDefinitionBuilder channelSpell(ResourceLocation id,
                                                        String name,
                                                        String description,
                                                        ResourceLocation slotFamily,
                                                        AbilityTargetResolverDefinition targeting,
                                                        int tickIntervalTicks,
                                                        boolean preserveInitialTargets,
                                                        @Nullable AbilityTargetResolverDefinition tickTargeting) {
        return AbilityDefinitionBuilder.create(id, name, description, slotFamily)
                .tag(AbilityDatagenKeys.TAG_SPELL)
                .tag(AbilityDatagenKeys.TAG_CHANNEL)
                .activation(CAST_ACTIVATION_ID, new AbilityActivationDefinition(
                        ActivationKind.MANUAL,
                        CAST_ENTRY_POINT,
                        targeting,
                        List.of(),
                        List.of(),
                        null,
                        0,
                        false,
                        STANDARD_MANUAL_INTERRUPT,
                        InterruptRefundPolicy.NONE,
                        new ActivationBehavior.ChannelBehavior(
                                tickIntervalTicks,
                                CHANNEL_TICK_ENTRY_POINT,
                                preserveInitialTargets,
                                tickTargeting
                        )
                ))
                .entryPoint(CAST_ENTRY_POINT, List.of())
                .entryPoint(CHANNEL_TICK_ENTRY_POINT, List.of());
    }

    public static AbilityDefinitionBuilder toggleAura(ResourceLocation id,
                                                      String name,
                                                      String description,
                                                      int pulseIntervalTicks,
                                                      AbilityTargetResolverDefinition pulseTargeting,
                                                      boolean pulseOnEnable) {
        return toggleAura(id, name, description, AbilityDatagenKeys.SLOT_FAMILY_BASIC, pulseIntervalTicks,
                pulseTargeting, pulseOnEnable);
    }

    public static AbilityDefinitionBuilder toggleAura(ResourceLocation id,
                                                      String name,
                                                      String description,
                                                      ResourceLocation slotFamily,
                                                      int pulseIntervalTicks,
                                                      AbilityTargetResolverDefinition pulseTargeting,
                                                      boolean pulseOnEnable) {
        return AbilityDefinitionBuilder.create(id, name, description, slotFamily)
                .tag(AbilityDatagenKeys.TAG_SPELL)
                .tag(AbilityDatagenKeys.TAG_TOGGLE)
                .tag(AbilityDatagenKeys.TAG_AURA)
                .activation(TOGGLE_ENABLE_ACTIVATION_ID, new AbilityActivationDefinition(
                        ActivationKind.TOGGLE_ENABLE,
                        TOGGLE_ENABLE_ENTRY_POINT,
                        AbilityDatagenKeys.TARGET_SELF,
                        List.of(),
                        List.of(),
                        null,
                        0,
                        false,
                        STANDARD_MANUAL_INTERRUPT,
                        InterruptRefundPolicy.NONE,
                        new ActivationBehavior.AuraBehavior(
                                pulseIntervalTicks,
                                AURA_PULSE_ENTRY_POINT,
                                pulseTargeting,
                                pulseOnEnable
                        )
                ))
                .activation(TOGGLE_DISABLE_ACTIVATION_ID, new AbilityActivationDefinition(
                        ActivationKind.TOGGLE_DISABLE,
                        TOGGLE_DISABLE_ENTRY_POINT,
                        AbilityDatagenKeys.TARGET_SELF,
                        List.of(),
                        List.of(),
                        null,
                        0,
                        false,
                        INTERNAL_INTERRUPT,
                        InterruptRefundPolicy.NONE,
                        new ActivationBehavior.InstantBehavior()
                ))
                .entryPoint(TOGGLE_ENABLE_ENTRY_POINT, List.of())
                .entryPoint(AURA_PULSE_ENTRY_POINT, List.of())
                .entryPoint(TOGGLE_DISABLE_ENTRY_POINT, List.of());
    }
}
