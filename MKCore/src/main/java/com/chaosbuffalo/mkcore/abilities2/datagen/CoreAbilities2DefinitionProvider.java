package com.chaosbuffalo.mkcore.abilities2.datagen;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities2.actions.AbilityAction;
import com.chaosbuffalo.mkcore.abilities2.actions.AbilityEventFilter;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityActivationDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityCostDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityCooldownDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityDefinitionData;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityDeliveryDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityParameterDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityPresentation;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityReactionDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityScalar;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityValue;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityValueKind;
import com.chaosbuffalo.mkcore.abilities2.definition.ActivationBehavior;
import com.chaosbuffalo.mkcore.abilities2.definition.ActivationKind;
import com.chaosbuffalo.mkcore.abilities2.definition.CostKind;
import com.chaosbuffalo.mkcore.abilities2.definition.DeliveryKind;
import com.chaosbuffalo.mkcore.abilities2.definition.InterruptRefundPolicy;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.init.CoreEntities;
import com.chaosbuffalo.mkcore.abilities2.runtime.AbilityEventType;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Map;

public class CoreAbilities2DefinitionProvider extends AbilityDefinitionProvider {
    public CoreAbilities2DefinitionProvider(DataGenerator generator) {
        super(generator, MKCore.MOD_ID);
    }

    @Override
    protected void addDefinitions() {
        AbilityDefinitionData selfHeal = createSelfHeal();
        add(selfHeal);
        add(AbilityVariants.variant(
                selfHeal,
                MKCore.makeRL("test_abilities2_greater_self_heal"),
                new AbilityPresentation(
                        "Abilities2 Greater Self Heal",
                        "A larger self-heal variant emitted from the same archetype shape.",
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                Map.of("amount", new AbilityValue.FloatValue(24.0f))
        ));
        add(createFirebolt());
        add(createCooldownProbe());
        add(createCostProbe());
        AbilityDefinitionData gcdSharedProbe = createGcdSharedProbe();
        add(gcdSharedProbe);
        add(AbilityVariants.variant(
                gcdSharedProbe,
                MKCore.makeRL("test_abilities2_gcd_probe_shared_b"),
                new AbilityPresentation(
                        "Abilities2 GCD Probe Shared B",
                        "A second probe in the same shared GCD bucket.",
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                Map.of("amount", new AbilityValue.FloatValue(3.0f))
        ));
        add(createGcdOtherProbe());
        add(createSpellCritPassive());
        add(createMendingChannel());
        add(createRestoringAura());
    }

    private AbilityDefinitionData createSelfHeal() {
        AbilityDefinitionBuilder builder = AbilityArchetypes.singleTargetSpell(
                        MKCore.makeRL("test_abilities2_self_heal"),
                        "Abilities2 Self Heal",
                        "A simple self-targeted heal built from the single-target archetype.",
                        AbilityDatagenKeys.TARGET_SELF
                )
                .school(AbilityDatagenKeys.SCHOOL_RESTORATION)
                .tag(AbilityDatagenKeys.TAG_HEAL)
                .parameter(floatParameter("amount", 12.0f, "Heal amount"));

        builder.activation(AbilityArchetypes.CAST_ACTIVATION_ID, new AbilityActivationDefinition(
                ActivationKind.MANUAL,
                AbilityArchetypes.CAST_ENTRY_POINT,
                AbilityDatagenKeys.TARGET_SELF,
                List.of(),
                List.of(),
                null,
                20,
                true,
                AbilityArchetypes.STANDARD_MANUAL_INTERRUPT,
                InterruptRefundPolicy.REFUND_COSTS_BEFORE_FIRST_EFFECT,
                new ActivationBehavior.InstantBehavior()
        ));
        builder.entryPoint(AbilityArchetypes.CAST_ENTRY_POINT, List.of(
                new AbilityAction.HealAction(
                        AbilityAction.ActionTarget.PRIMARY_ENTITY,
                        new AbilityScalar.ParameterScalar("amount")
                )
        ));
        return builder.build();
    }

    private AbilityDefinitionData createFirebolt() {
        AbilityDefinitionBuilder builder = AbilityArchetypes.projectileSpell(
                        MKCore.makeRL("test_abilities2_firebolt"),
                        "Abilities2 Firebolt",
                        "Launches a projectile forward and detonates on impact.",
                        AbilityDatagenKeys.TARGET_SELF,
                        AbilityAction.ActionTarget.SELF,
                        new AbilityScalar.ConstantScalar(1.6),
                        new AbilityScalar.ConstantScalar(0.0)
                )
                .school(AbilityDatagenKeys.SCHOOL_EVOCATION)
                .tag(AbilityDatagenKeys.TAG_FIRE)
                .parameter(floatParameter("impact_damage", 8.0f, "Projectile impact damage"));

        builder.delivery(AbilityArchetypes.PROJECTILE_DELIVERY_ID, new AbilityDeliveryDefinition(
                DeliveryKind.PROJECTILE,
                CoreEntities.ABILITY_PROJECTILE_TYPE.getId(),
                Items.FIRE_CHARGE.builtInRegistryHolder().key().location(),
                List.of(),
                AbilityArchetypes.PROJECTILE_IMPACT_ACTIVATION_ID,
                null,
                null
        ));

        builder.entryPoint(AbilityArchetypes.PROJECTILE_IMPACT_ENTRY_POINT, List.of(
                new AbilityAction.DamageAction(
                        AbilityAction.ActionTarget.PRIMARY_ENTITY,
                        new AbilityScalar.ParameterScalar("impact_damage"),
                        CoreDamageTypes.FireDamage.getId()
                )
        ));
        return builder.build();
    }

    private AbilityDefinitionData createCooldownProbe() {
        AbilityDefinitionBuilder builder = AbilityArchetypes.singleTargetSpell(
                        MKCore.makeRL("test_abilities2_cooldown_probe"),
                        "Abilities2 Cooldown Probe",
                        "A simple self-targeted probe used to verify synced loadout cooldown timers.",
                        AbilityDatagenKeys.TARGET_SELF
                )
                .school(AbilityDatagenKeys.SCHOOL_RESTORATION)
                .tag(AbilityDatagenKeys.TAG_SPELL)
                .parameter(floatParameter("amount", 2.0f, "Probe heal amount"));

        builder.activation(AbilityArchetypes.CAST_ACTIVATION_ID, new AbilityActivationDefinition(
                ActivationKind.MANUAL,
                AbilityArchetypes.CAST_ENTRY_POINT,
                AbilityDatagenKeys.TARGET_SELF,
                List.of(),
                List.of(new AbilityCooldownDefinition(
                        com.chaosbuffalo.mkcore.abilities2.definition.StateScope.ABILITY_FAMILY,
                        "ability",
                        new AbilityScalar.ConstantScalar(40.0)
                )),
                null,
                0,
                false,
                AbilityArchetypes.STANDARD_MANUAL_INTERRUPT,
                InterruptRefundPolicy.NONE,
                new ActivationBehavior.InstantBehavior()
        ));
        builder.entryPoint(AbilityArchetypes.CAST_ENTRY_POINT, List.of(
                new AbilityAction.HealAction(
                        AbilityAction.ActionTarget.PRIMARY_ENTITY,
                        new AbilityScalar.ParameterScalar("amount")
                )
        ));
        return builder.build();
    }

    private AbilityDefinitionData createCostProbe() {
        AbilityDefinitionBuilder builder = AbilityArchetypes.singleTargetSpell(
                        MKCore.makeRL("test_abilities2_cost_probe"),
                        "Abilities2 Cost Probe",
                        "A simple self-targeted probe used to verify client-side cost gating for loadout abilities.",
                        AbilityDatagenKeys.TARGET_SELF
                )
                .school(AbilityDatagenKeys.SCHOOL_RESTORATION)
                .tag(AbilityDatagenKeys.TAG_SPELL)
                .parameter(floatParameter("amount", 2.0f, "Probe heal amount"));

        builder.activation(AbilityArchetypes.CAST_ACTIVATION_ID, new AbilityActivationDefinition(
                ActivationKind.MANUAL,
                AbilityArchetypes.CAST_ENTRY_POINT,
                AbilityDatagenKeys.TARGET_SELF,
                List.of(new AbilityCostDefinition(
                        CostKind.MANA,
                        null,
                        new AbilityScalar.ConstantScalar(20.0)
                )),
                List.of(),
                null,
                0,
                false,
                AbilityArchetypes.STANDARD_MANUAL_INTERRUPT,
                InterruptRefundPolicy.NONE,
                new ActivationBehavior.InstantBehavior()
        ));
        builder.entryPoint(AbilityArchetypes.CAST_ENTRY_POINT, List.of(
                new AbilityAction.HealAction(
                        AbilityAction.ActionTarget.PRIMARY_ENTITY,
                        new AbilityScalar.ParameterScalar("amount")
                )
        ));
        return builder.build();
    }

    private AbilityDefinitionData createGcdSharedProbe() {
        return createGcdProbe(
                MKCore.makeRL("test_abilities2_gcd_probe_shared_a"),
                "Abilities2 GCD Probe Shared A",
                "A simple self-targeted probe used to verify shared abilities2 GCD synchronization.",
                MKCore.makeRL("gcd_probe.shared")
        );
    }

    private AbilityDefinitionData createGcdOtherProbe() {
        return createGcdProbe(
                MKCore.makeRL("test_abilities2_gcd_probe_other"),
                "Abilities2 GCD Probe Other",
                "A simple self-targeted probe used to verify independent abilities2 GCD groups.",
                MKCore.makeRL("gcd_probe.other")
        );
    }

    private AbilityDefinitionData createGcdProbe(net.minecraft.resources.ResourceLocation id,
                                                 String name,
                                                 String description,
                                                 net.minecraft.resources.ResourceLocation gcdGroup) {
        AbilityDefinitionBuilder builder = AbilityArchetypes.singleTargetSpell(
                        id,
                        name,
                        description,
                        AbilityDatagenKeys.TARGET_SELF
                )
                .school(AbilityDatagenKeys.SCHOOL_RESTORATION)
                .tag(AbilityDatagenKeys.TAG_SPELL)
                .parameter(floatParameter("amount", 2.0f, "Probe heal amount"));

        builder.activation(AbilityArchetypes.CAST_ACTIVATION_ID, new AbilityActivationDefinition(
                ActivationKind.MANUAL,
                AbilityArchetypes.CAST_ENTRY_POINT,
                AbilityDatagenKeys.TARGET_SELF,
                List.of(),
                List.of(),
                gcdGroup,
                0,
                false,
                AbilityArchetypes.STANDARD_MANUAL_INTERRUPT,
                InterruptRefundPolicy.NONE,
                new ActivationBehavior.InstantBehavior()
        ));
        builder.entryPoint(AbilityArchetypes.CAST_ENTRY_POINT, List.of(
                new AbilityAction.HealAction(
                        AbilityAction.ActionTarget.PRIMARY_ENTITY,
                        new AbilityScalar.ParameterScalar("amount")
                )
        ));
        return builder.build();
    }

    private AbilityDefinitionData createSpellCritPassive() {
        AbilityReactionDefinition reaction = new AbilityReactionDefinition(
                AbilityEventType.SPELL_CRIT,
                List.of(new AbilityEventFilter.EventSourceTagFilter(AbilityDatagenKeys.TAG_SPELL)),
                1.0f,
                20,
                true,
                2,
                AbilityArchetypes.PROC_ACTIVATION_ID
        );

        AbilityDefinitionBuilder builder = AbilityArchetypes.passiveProc(
                        MKCore.makeRL("test_abilities2_spell_crit_passive"),
                        "Abilities2 Spell Crit Passive",
                        "Installs a reaction that splashes extra fire damage on spell crits.",
                        reaction,
                        AbilityDatagenKeys.TARGET_EVENT_TARGET
                )
                .school(AbilityDatagenKeys.SCHOOL_EVOCATION)
                .tag(AbilityDatagenKeys.TAG_FIRE)
                .parameter(floatParameter("proc_damage", 3.0f, "Spell crit proc damage"));

        builder.entryPoint(AbilityArchetypes.PROC_ENTRY_POINT, List.of(
                new AbilityAction.DamageAction(
                        AbilityAction.ActionTarget.PRIMARY_ENTITY,
                        new AbilityScalar.ParameterScalar("proc_damage"),
                        CoreDamageTypes.FireDamage.getId()
                )
        ));
        return builder.build();
    }

    private AbilityDefinitionData createMendingChannel() {
        AbilityDefinitionBuilder builder = AbilityArchetypes.channelSpell(
                        MKCore.makeRL("test_abilities2_mending_channel"),
                        "Abilities2 Mending Channel",
                        "Begins with a heal, then pulses more healing while the channel is maintained.",
                        AbilityDatagenKeys.TARGET_SELF,
                        20,
                        true,
                        null
                )
                .school(AbilityDatagenKeys.SCHOOL_RESTORATION)
                .tag(AbilityDatagenKeys.TAG_HEAL)
                .parameter(floatParameter("initial_heal", 4.0f, "Initial channel heal"))
                .parameter(floatParameter("tick_heal", 2.0f, "Per-pulse channel heal"));

        builder.entryPoint(AbilityArchetypes.CAST_ENTRY_POINT, List.of(
                new AbilityAction.HealAction(
                        AbilityAction.ActionTarget.PRIMARY_ENTITY,
                        new AbilityScalar.ParameterScalar("initial_heal")
                )
        ));
        builder.entryPoint(AbilityArchetypes.CHANNEL_TICK_ENTRY_POINT, List.of(
                new AbilityAction.HealAction(
                        AbilityAction.ActionTarget.PRIMARY_ENTITY,
                        new AbilityScalar.ParameterScalar("tick_heal")
                )
        ));
        return builder.build();
    }

    private AbilityDefinitionData createRestoringAura() {
        AbilityDefinitionBuilder builder = AbilityArchetypes.toggleAura(
                        MKCore.makeRL("test_abilities2_restoring_aura"),
                        "Abilities2 Restoring Aura",
                        "A toggle aura that repeatedly heals the caster.",
                        40,
                        AbilityDatagenKeys.TARGET_SELF,
                        true
                )
                .school(AbilityDatagenKeys.SCHOOL_RESTORATION)
                .tag(AbilityDatagenKeys.TAG_HEAL)
                .parameter(floatParameter("pulse_heal", 2.0f, "Aura pulse heal"));

        builder.entryPoint(AbilityArchetypes.AURA_PULSE_ENTRY_POINT, List.of(
                new AbilityAction.HealAction(
                        AbilityAction.ActionTarget.PRIMARY_ENTITY,
                        new AbilityScalar.ParameterScalar("pulse_heal")
                )
        ));
        return builder.build();
    }

    private AbilityParameterDefinition floatParameter(String id, float defaultValue, String description) {
        return new AbilityParameterDefinition(
                id,
                new AbilityValue.FloatValue(defaultValue),
                AbilityValueKind.FLOAT,
                true,
                true,
                description
        );
    }
}
