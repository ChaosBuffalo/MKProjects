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
        add(createProjectileGround());
        add(createAiSelfHeal());
        add(createAiFirebolt());
        add(createFriendlyHeal());
        add(createCooldownProbe());
        add(createCostProbe());
        add(createDelayedBurst());
        add(createHealingCloud());
        add(createFireCloud());
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
                null,
                null,
                null,
                null,
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

    private AbilityDefinitionData createProjectileGround() {
        AbilityDefinitionBuilder builder = AbilityArchetypes.projectileSpell(
                        MKCore.makeRL("test_abilities2_projectile_ground"),
                        "Abilities2 Projectile Ground",
                        "A self-targeted projectile fixture used to verify grounded projectile callback persistence.",
                        AbilityDatagenKeys.TARGET_SELF,
                        AbilityAction.ActionTarget.SELF,
                        new AbilityScalar.ConstantScalar(1.0),
                        new AbilityScalar.ConstantScalar(0.0)
                )
                .school(AbilityDatagenKeys.SCHOOL_EVOCATION)
                .tag(AbilityDatagenKeys.TAG_FIRE)
                .parameter(floatParameter("ground_damage", 2.0f, "Ground callback damage"));

        builder.delivery(AbilityArchetypes.PROJECTILE_DELIVERY_ID, new AbilityDeliveryDefinition(
                DeliveryKind.PROJECTILE,
                CoreEntities.ABILITY_PROJECTILE_TYPE.getId(),
                Items.SNOWBALL.builtInRegistryHolder().key().location(),
                List.of(),
                null,
                null,
                null,
                null,
                null,
                null,
                "ground_proc"
        ));
        builder.activation("ground_proc", new AbilityActivationDefinition(
                ActivationKind.PROC,
                "ground_proc",
                AbilityDatagenKeys.TARGET_SELF,
                List.of(),
                List.of(),
                null,
                0,
                false,
                AbilityArchetypes.INTERNAL_INTERRUPT,
                InterruptRefundPolicy.NONE,
                new ActivationBehavior.InstantBehavior()
        ));
        builder.entryPoint("ground_proc", List.of(
                new AbilityAction.DamageAction(
                        AbilityAction.ActionTarget.SELF,
                        new AbilityScalar.ParameterScalar("ground_damage"),
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

    private AbilityDefinitionData createAiSelfHeal() {
        AbilityDefinitionBuilder builder = AbilityArchetypes.singleTargetSpell(
                        MKCore.makeRL("test_abilities2_ai_self_heal"),
                        "Abilities2 AI Self Heal",
                        "A self-targeted AI activation used to validate NPC execution of abilities2 definitions.",
                        AbilityDatagenKeys.TARGET_SELF
                )
                .school(AbilityDatagenKeys.SCHOOL_RESTORATION)
                .tag(AbilityDatagenKeys.TAG_HEAL)
                .parameter(floatParameter("amount", 8.0f, "Heal amount"));

        builder.activation(AbilityArchetypes.CAST_ACTIVATION_ID, new AbilityActivationDefinition(
                ActivationKind.AI,
                AbilityArchetypes.CAST_ENTRY_POINT,
                AbilityDatagenKeys.TARGET_SELF,
                List.of(),
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

    private AbilityDefinitionData createAiFirebolt() {
        AbilityDefinitionBuilder builder = AbilityArchetypes.projectileSpell(
                        MKCore.makeRL("test_abilities2_ai_firebolt"),
                        "Abilities2 AI Firebolt",
                        "A threat-targeted AI projectile activation used to validate NPC selection of abilities2 definitions.",
                        AbilityDatagenKeys.TARGET_RESOLVED_ENEMY,
                        AbilityAction.ActionTarget.PRIMARY_ENTITY,
                        new AbilityScalar.ConstantScalar(1.6),
                        new AbilityScalar.ConstantScalar(0.0)
                )
                .school(AbilityDatagenKeys.SCHOOL_EVOCATION)
                .tag(AbilityDatagenKeys.TAG_FIRE)
                .parameter(floatParameter("impact_damage", 6.0f, "Projectile impact damage"));

        builder.activation(AbilityArchetypes.CAST_ACTIVATION_ID, new AbilityActivationDefinition(
                ActivationKind.AI,
                AbilityArchetypes.CAST_ENTRY_POINT,
                AbilityDatagenKeys.TARGET_RESOLVED_ENEMY,
                List.of(),
                List.of(),
                null,
                0,
                false,
                AbilityArchetypes.STANDARD_MANUAL_INTERRUPT,
                InterruptRefundPolicy.NONE,
                new ActivationBehavior.InstantBehavior()
        ));
        builder.delivery(AbilityArchetypes.PROJECTILE_DELIVERY_ID, new AbilityDeliveryDefinition(
                DeliveryKind.PROJECTILE,
                CoreEntities.ABILITY_PROJECTILE_TYPE.getId(),
                Items.FIRE_CHARGE.builtInRegistryHolder().key().location(),
                List.of(),
                null,
                null,
                null,
                null,
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

    private AbilityDefinitionData createFriendlyHeal() {
        AbilityDefinitionBuilder builder = AbilityArchetypes.singleTargetSpell(
                        MKCore.makeRL("test_abilities2_friendly_heal"),
                        "Abilities2 Friendly Heal",
                        "A friendly-targeted direct heal used to validate relation-aware forced target checks.",
                        AbilityDatagenKeys.TARGET_RESOLVED_FRIENDLY
                )
                .school(AbilityDatagenKeys.SCHOOL_RESTORATION)
                .tag(AbilityDatagenKeys.TAG_HEAL)
                .parameter(floatParameter("amount", 10.0f, "Heal amount"));

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

    private AbilityDefinitionData createDelayedBurst() {
        AbilityDefinitionBuilder builder = AbilityDefinitionBuilder.create(
                        MKCore.makeRL("test_abilities2_delayed_burst"),
                        "Abilities2 Delayed Burst",
                        "Stages a delayed ground burst under the selected target before detonating.",
                        AbilityDatagenKeys.SLOT_FAMILY_BASIC
                )
                .school(AbilityDatagenKeys.SCHOOL_EVOCATION)
                .tag(AbilityDatagenKeys.TAG_SPELL)
                .tag(AbilityDatagenKeys.TAG_FIRE)
                .parameter(floatParameter("impact_damage", 6.0f, "Burst impact damage"));

        builder.activation("cast", new AbilityActivationDefinition(
                ActivationKind.MANUAL,
                "cast",
                AbilityDatagenKeys.TARGET_RESOLVED_ENEMY,
                List.of(),
                List.of(),
                null,
                0,
                false,
                AbilityArchetypes.STANDARD_MANUAL_INTERRUPT,
                InterruptRefundPolicy.NONE,
                new ActivationBehavior.InstantBehavior()
        ));
        builder.activation("burst_impact", new AbilityActivationDefinition(
                ActivationKind.PROC,
                "burst_impact",
                AbilityDatagenKeys.TARGET_RESOLVED_ENEMY,
                List.of(),
                List.of(),
                null,
                0,
                false,
                AbilityArchetypes.INTERNAL_INTERRUPT,
                InterruptRefundPolicy.NONE,
                new ActivationBehavior.InstantBehavior()
        ));
        builder.entryPoint("cast", List.of(
                new AbilityAction.StartDeliveryAction("burst", AbilityAction.ActionTarget.PRIMARY_ENTITY)
        ));
        builder.entryPoint("burst_impact", List.of(
                new AbilityAction.DamageAction(
                        AbilityAction.ActionTarget.PRIMARY_ENTITY,
                        new AbilityScalar.ParameterScalar("impact_damage"),
                        CoreDamageTypes.FireDamage.getId()
                )
        ));
        builder.delivery("burst", new AbilityDeliveryDefinition(
                DeliveryKind.DELAYED_GROUND_BURST,
                null,
                null,
                List.of(),
                new AbilityScalar.ConstantScalar(4.0),
                null,
                null,
                new AbilityScalar.ConstantScalar(1.5),
                "burst_impact",
                null,
                null
        ));
        return builder.build();
    }

    private AbilityDefinitionData createHealingCloud() {
        AbilityDefinitionBuilder builder = AbilityDefinitionBuilder.create(
                        MKCore.makeRL("test_abilities2_healing_cloud"),
                        "Abilities2 Healing Cloud",
                        "Creates a short-lived healing cloud that pulses around the caster.",
                        AbilityDatagenKeys.SLOT_FAMILY_BASIC
                )
                .school(AbilityDatagenKeys.SCHOOL_RESTORATION)
                .tag(AbilityDatagenKeys.TAG_SPELL)
                .tag(AbilityDatagenKeys.TAG_HEAL)
                .parameter(floatParameter("tick_heal", 2.0f, "Cloud pulse heal"));

        builder.activation("cast", new AbilityActivationDefinition(
                ActivationKind.MANUAL,
                "cast",
                AbilityDatagenKeys.TARGET_SELF,
                List.of(),
                List.of(),
                null,
                0,
                false,
                AbilityArchetypes.STANDARD_MANUAL_INTERRUPT,
                InterruptRefundPolicy.NONE,
                new ActivationBehavior.InstantBehavior()
        ));
        builder.activation("cloud_tick", new AbilityActivationDefinition(
                ActivationKind.PROC,
                "cloud_tick",
                AbilityDatagenKeys.TARGET_RESOLVED_FRIENDLY,
                List.of(),
                List.of(),
                null,
                0,
                false,
                AbilityArchetypes.INTERNAL_INTERRUPT,
                InterruptRefundPolicy.NONE,
                new ActivationBehavior.InstantBehavior()
        ));
        builder.entryPoint("cast", List.of(
                new AbilityAction.StartDeliveryAction("cloud", AbilityAction.ActionTarget.SELF)
        ));
        builder.entryPoint("cloud_tick", List.of(
                new AbilityAction.HealAction(
                        AbilityAction.ActionTarget.PRIMARY_ENTITY,
                        new AbilityScalar.ParameterScalar("tick_heal")
                )
        ));
        builder.delivery("cloud", new AbilityDeliveryDefinition(
                DeliveryKind.AREA_CLOUD,
                null,
                null,
                List.of(),
                null,
                new AbilityScalar.ConstantScalar(14.0),
                new AbilityScalar.ConstantScalar(4.0),
                new AbilityScalar.ConstantScalar(2.0),
                null,
                null,
                "cloud_tick"
        ));
        return builder.build();
    }

    private AbilityDefinitionData createFireCloud() {
        AbilityDefinitionBuilder builder = AbilityDefinitionBuilder.create(
                        MKCore.makeRL("test_abilities2_fire_cloud"),
                        "Abilities2 Fire Cloud",
                        "Creates a short-lived damage cloud that should only select enemies in range.",
                        AbilityDatagenKeys.SLOT_FAMILY_BASIC
                )
                .school(AbilityDatagenKeys.SCHOOL_EVOCATION)
                .tag(AbilityDatagenKeys.TAG_SPELL)
                .tag(AbilityDatagenKeys.TAG_FIRE)
                .parameter(floatParameter("tick_damage", 2.0f, "Cloud pulse damage"));

        builder.activation("cast", new AbilityActivationDefinition(
                ActivationKind.MANUAL,
                "cast",
                AbilityDatagenKeys.TARGET_SELF,
                List.of(),
                List.of(),
                null,
                0,
                false,
                AbilityArchetypes.STANDARD_MANUAL_INTERRUPT,
                InterruptRefundPolicy.NONE,
                new ActivationBehavior.InstantBehavior()
        ));
        builder.activation("cloud_tick", new AbilityActivationDefinition(
                ActivationKind.PROC,
                "cloud_tick",
                AbilityDatagenKeys.TARGET_RESOLVED_ENEMY,
                List.of(),
                List.of(),
                null,
                0,
                false,
                AbilityArchetypes.INTERNAL_INTERRUPT,
                InterruptRefundPolicy.NONE,
                new ActivationBehavior.InstantBehavior()
        ));
        builder.entryPoint("cast", List.of(
                new AbilityAction.StartDeliveryAction("cloud", AbilityAction.ActionTarget.SELF)
        ));
        builder.entryPoint("cloud_tick", List.of(
                new AbilityAction.DamageAction(
                        AbilityAction.ActionTarget.PRIMARY_ENTITY,
                        new AbilityScalar.ParameterScalar("tick_damage"),
                        CoreDamageTypes.FireDamage.getId()
                )
        ));
        builder.delivery("cloud", new AbilityDeliveryDefinition(
                DeliveryKind.AREA_CLOUD,
                null,
                null,
                List.of(),
                null,
                new AbilityScalar.ConstantScalar(14.0),
                new AbilityScalar.ConstantScalar(4.0),
                new AbilityScalar.ConstantScalar(2.0),
                null,
                null,
                "cloud_tick"
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
