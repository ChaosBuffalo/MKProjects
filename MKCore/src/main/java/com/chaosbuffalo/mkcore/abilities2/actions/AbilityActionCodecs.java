package com.chaosbuffalo.mkcore.abilities2.actions;

import com.chaosbuffalo.mkcore.abilities2.codec.AbilityCodecs;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityCostDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityScalar;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityValue;
import com.chaosbuffalo.mkcore.abilities2.definition.StateScope;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public final class AbilityActionCodecs {
    private AbilityActionCodecs() {
    }

    public static final Codec<AbilityAction.ActionTarget> ACTION_TARGET_CODEC =
            AbilityCodecs.enumCodec(AbilityAction.ActionTarget.class);
    public static final Codec<AbilityAction.TargetSet> TARGET_SET_CODEC =
            AbilityCodecs.enumCodec(AbilityAction.TargetSet.class);
    public static final Codec<AbilityAction.ModifyStateOperation> MODIFY_STATE_OPERATION_CODEC =
            AbilityCodecs.enumCodec(AbilityAction.ModifyStateOperation.class);

    public static final Codec<AbilityConditionDefinition> CONDITION_CODEC =
            AbilityCodecs.typedJsonObjectCodec("type", AbilityConditionDefinition::type,
                    AbilityConditionDefinition::data, AbilityConditionDefinition::new, "ability condition");

    private static Codec<java.util.List<AbilityAction>> actionListCodec() {
        return Codec.lazyInitialized(() -> ACTION_CODEC.listOf());
    }

    private static final MapCodec<AbilityAction.DamageAction> DAMAGE_ACTION_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ACTION_TARGET_CODEC.fieldOf("target").forGetter(AbilityAction.DamageAction::target),
            AbilityCodecs.ABILITY_SCALAR_CODEC.fieldOf("amount").forGetter(AbilityAction.DamageAction::amount),
            ResourceLocation.CODEC.optionalFieldOf("school").forGetter(action ->
                    java.util.Optional.ofNullable(action.school()))
    ).apply(builder, (target, amount, school) -> new AbilityAction.DamageAction(target, amount, school.orElse(null))));

    private static final MapCodec<AbilityAction.HealAction> HEAL_ACTION_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ACTION_TARGET_CODEC.fieldOf("target").forGetter(AbilityAction.HealAction::target),
            AbilityCodecs.ABILITY_SCALAR_CODEC.fieldOf("amount").forGetter(AbilityAction.HealAction::amount)
    ).apply(builder, AbilityAction.HealAction::new));

    private static final MapCodec<AbilityAction.ApplyEffectAction> APPLY_EFFECT_ACTION_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ACTION_TARGET_CODEC.fieldOf("target").forGetter(AbilityAction.ApplyEffectAction::target),
            ResourceLocation.CODEC.fieldOf("effect").forGetter(AbilityAction.ApplyEffectAction::effect),
            AbilityCodecs.ABILITY_SCALAR_CODEC.optionalFieldOf("duration").forGetter(action ->
                    java.util.Optional.ofNullable(action.duration())),
            Codec.INT.optionalFieldOf("stack_count", 1).forGetter(AbilityAction.ApplyEffectAction::stackCount)
    ).apply(builder, (target, effect, duration, stackCount) ->
            new AbilityAction.ApplyEffectAction(target, effect, duration.orElse(null), stackCount)));

    private static final MapCodec<AbilityAction.ModifyStateAction> MODIFY_STATE_ACTION_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            AbilityCodecs.STATE_SCOPE_CODEC.fieldOf("scope").forGetter(AbilityAction.ModifyStateAction::scope),
            Codec.STRING.fieldOf("state_key").forGetter(AbilityAction.ModifyStateAction::stateKey),
            MODIFY_STATE_OPERATION_CODEC.fieldOf("operation").forGetter(AbilityAction.ModifyStateAction::operation),
            AbilityCodecs.ABILITY_VALUE_CODEC.optionalFieldOf("value").forGetter(action ->
                    java.util.Optional.ofNullable(action.value()))
    ).apply(builder, (scope, stateKey, operation, value) ->
            new AbilityAction.ModifyStateAction(scope, stateKey, operation, value.orElse(null))));

    private static final MapCodec<AbilityAction.PayCostAction> PAY_COST_ACTION_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            AbilityCodecs.ABILITY_COST_CODEC.fieldOf("cost").forGetter(AbilityAction.PayCostAction::cost)
    ).apply(builder, AbilityAction.PayCostAction::new));

    private static final MapCodec<AbilityAction.SetVarAction> SET_VAR_ACTION_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.STRING.fieldOf("name").forGetter(AbilityAction.SetVarAction::name),
            AbilityCodecs.ABILITY_VALUE_CODEC.fieldOf("value").forGetter(AbilityAction.SetVarAction::value)
    ).apply(builder, AbilityAction.SetVarAction::new));

    private static final MapCodec<AbilityAction.BranchAction> BRANCH_ACTION_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            CONDITION_CODEC.fieldOf("condition").forGetter(AbilityAction.BranchAction::condition),
            actionListCodec().fieldOf("if_true").forGetter(AbilityAction.BranchAction::ifTrue),
            actionListCodec().optionalFieldOf("if_false", java.util.List.of()).forGetter(AbilityAction.BranchAction::ifFalse)
    ).apply(builder, AbilityAction.BranchAction::new));

    private static final MapCodec<AbilityAction.ForEachTargetAction> FOR_EACH_TARGET_ACTION_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            TARGET_SET_CODEC.fieldOf("targets").forGetter(AbilityAction.ForEachTargetAction::targets),
            actionListCodec().fieldOf("actions").forGetter(AbilityAction.ForEachTargetAction::actions)
    ).apply(builder, AbilityAction.ForEachTargetAction::new));

    private static final MapCodec<AbilityAction.StartEntryPointAction> START_ENTRY_POINT_ACTION_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.STRING.fieldOf("entry_point").forGetter(AbilityAction.StartEntryPointAction::entryPoint)
    ).apply(builder, AbilityAction.StartEntryPointAction::new));

    private static final MapCodec<AbilityAction.InstallReactionAction> INSTALL_REACTION_ACTION_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.STRING.fieldOf("reaction").forGetter(AbilityAction.InstallReactionAction::reaction)
    ).apply(builder, AbilityAction.InstallReactionAction::new));

    private static final MapCodec<AbilityAction.RemoveReactionAction> REMOVE_REACTION_ACTION_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.STRING.fieldOf("reaction").forGetter(AbilityAction.RemoveReactionAction::reaction)
    ).apply(builder, AbilityAction.RemoveReactionAction::new));

    private static final MapCodec<AbilityAction.SpawnProjectileAction> SPAWN_PROJECTILE_ACTION_CODEC =
            RecordCodecBuilder.mapCodec(builder -> builder.group(
                    Codec.STRING.fieldOf("delivery").forGetter(AbilityAction.SpawnProjectileAction::delivery),
                    ACTION_TARGET_CODEC.optionalFieldOf("target", AbilityAction.ActionTarget.PRIMARY_ENTITY)
                            .forGetter(AbilityAction.SpawnProjectileAction::target),
                    AbilityCodecs.ABILITY_SCALAR_CODEC.fieldOf("speed").forGetter(AbilityAction.SpawnProjectileAction::speed),
                    AbilityCodecs.ABILITY_SCALAR_CODEC.optionalFieldOf("inaccuracy", new AbilityScalar.ConstantScalar(0.0))
                            .forGetter(AbilityAction.SpawnProjectileAction::inaccuracy)
            ).apply(builder, AbilityAction.SpawnProjectileAction::new));

    private static final MapCodec<AbilityAction.StartDeliveryAction> START_DELIVERY_ACTION_CODEC =
            RecordCodecBuilder.mapCodec(builder -> builder.group(
                    Codec.STRING.fieldOf("delivery").forGetter(AbilityAction.StartDeliveryAction::delivery),
                    ACTION_TARGET_CODEC.optionalFieldOf("target", AbilityAction.ActionTarget.PRIMARY_ENTITY)
                            .forGetter(AbilityAction.StartDeliveryAction::target)
            ).apply(builder, AbilityAction.StartDeliveryAction::new));

    public static final Codec<AbilityAction> ACTION_CODEC = Codec.lazyInitialized(() ->
            Codec.STRING.dispatch(AbilityAction::type, type -> switch (type) {
                case "damage" -> DAMAGE_ACTION_CODEC;
                case "heal" -> HEAL_ACTION_CODEC;
                case "apply_effect" -> APPLY_EFFECT_ACTION_CODEC;
                case "modify_state" -> MODIFY_STATE_ACTION_CODEC;
                case "pay_cost" -> PAY_COST_ACTION_CODEC;
                case "set_var" -> SET_VAR_ACTION_CODEC;
                case "branch" -> BRANCH_ACTION_CODEC;
                case "for_each_target" -> FOR_EACH_TARGET_ACTION_CODEC;
                case "start_entry_point" -> START_ENTRY_POINT_ACTION_CODEC;
                case "install_reaction" -> INSTALL_REACTION_ACTION_CODEC;
                case "remove_reaction" -> REMOVE_REACTION_ACTION_CODEC;
                case "spawn_projectile" -> SPAWN_PROJECTILE_ACTION_CODEC;
                case "start_delivery" -> START_DELIVERY_ACTION_CODEC;
                default -> throw new IllegalStateException("Unknown ability action type " + type);
            }));
}
