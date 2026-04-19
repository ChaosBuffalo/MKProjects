package com.chaosbuffalo.mkcore.abilities2.actions;

import com.chaosbuffalo.mkcore.abilities2.definition.AbilityCostDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityScalar;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityValue;
import com.chaosbuffalo.mkcore.abilities2.definition.StateScope;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public sealed interface AbilityAction permits AbilityAction.DamageAction, AbilityAction.HealAction,
        AbilityAction.ApplyEffectAction, AbilityAction.ModifyStateAction, AbilityAction.PayCostAction,
        AbilityAction.SetVarAction, AbilityAction.BranchAction, AbilityAction.ForEachTargetAction,
        AbilityAction.StartEntryPointAction, AbilityAction.InstallReactionAction,
        AbilityAction.RemoveReactionAction, AbilityAction.SpawnProjectileAction {

    String type();

    enum ActionTarget {
        SELF,
        PRIMARY_ENTITY,
        TARGET,
        EVENT_TARGET
    }

    enum TargetSet {
        SELECTED
    }

    enum ModifyStateOperation {
        SET_BOOL,
        SET_INT,
        ADD_INT,
        SET_FLOAT,
        ADD_FLOAT,
        CLEAR
    }

    record DamageAction(ActionTarget target,
                        AbilityScalar amount,
                        @Nullable ResourceLocation school) implements AbilityAction {
        public DamageAction {
            Objects.requireNonNull(target, "target");
            Objects.requireNonNull(amount, "amount");
        }

        @Override
        public String type() {
            return "damage";
        }
    }

    record HealAction(ActionTarget target,
                      AbilityScalar amount) implements AbilityAction {
        public HealAction {
            Objects.requireNonNull(target, "target");
            Objects.requireNonNull(amount, "amount");
        }

        @Override
        public String type() {
            return "heal";
        }
    }

    record ApplyEffectAction(ActionTarget target,
                             ResourceLocation effect,
                             @Nullable AbilityScalar duration,
                             int stackCount) implements AbilityAction {
        public ApplyEffectAction {
            Objects.requireNonNull(target, "target");
            Objects.requireNonNull(effect, "effect");
            if (stackCount <= 0) {
                throw new IllegalArgumentException("Apply effect stackCount must be > 0");
            }
        }

        @Override
        public String type() {
            return "apply_effect";
        }
    }

    record ModifyStateAction(StateScope scope,
                             String stateKey,
                             ModifyStateOperation operation,
                             @Nullable AbilityValue value) implements AbilityAction {
        public ModifyStateAction {
            Objects.requireNonNull(scope, "scope");
            if (stateKey == null || stateKey.isBlank()) {
                throw new IllegalArgumentException("Modify state stateKey must not be blank");
            }
            Objects.requireNonNull(operation, "operation");
            if (operation == ModifyStateOperation.CLEAR && value != null) {
                throw new IllegalArgumentException("Modify state CLEAR operation must not provide a value");
            }
            if (operation != ModifyStateOperation.CLEAR && value == null) {
                throw new IllegalArgumentException("Modify state operation %s requires a value".formatted(operation));
            }
        }

        @Override
        public String type() {
            return "modify_state";
        }
    }

    record PayCostAction(AbilityCostDefinition cost) implements AbilityAction {
        public PayCostAction {
            Objects.requireNonNull(cost, "cost");
        }

        @Override
        public String type() {
            return "pay_cost";
        }
    }

    record SetVarAction(String name,
                        AbilityValue value) implements AbilityAction {
        public SetVarAction {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Set var name must not be blank");
            }
            Objects.requireNonNull(value, "value");
        }

        @Override
        public String type() {
            return "set_var";
        }
    }

    record BranchAction(AbilityConditionDefinition condition,
                        List<AbilityAction> ifTrue,
                        List<AbilityAction> ifFalse) implements AbilityAction {
        public BranchAction {
            Objects.requireNonNull(condition, "condition");
            ifTrue = List.copyOf(Objects.requireNonNull(ifTrue, "ifTrue"));
            ifFalse = List.copyOf(Objects.requireNonNull(ifFalse, "ifFalse"));
        }

        @Override
        public String type() {
            return "branch";
        }
    }

    record ForEachTargetAction(TargetSet targets,
                               List<AbilityAction> actions) implements AbilityAction {
        public ForEachTargetAction {
            Objects.requireNonNull(targets, "targets");
            actions = List.copyOf(Objects.requireNonNull(actions, "actions"));
        }

        @Override
        public String type() {
            return "for_each_target";
        }
    }

    record StartEntryPointAction(String entryPoint) implements AbilityAction {
        public StartEntryPointAction {
            if (entryPoint == null || entryPoint.isBlank()) {
                throw new IllegalArgumentException("Start entry point entryPoint must not be blank");
            }
        }

        @Override
        public String type() {
            return "start_entry_point";
        }
    }

    record InstallReactionAction(String reaction) implements AbilityAction {
        public InstallReactionAction {
            if (reaction == null || reaction.isBlank()) {
                throw new IllegalArgumentException("Install reaction reaction must not be blank");
            }
        }

        @Override
        public String type() {
            return "install_reaction";
        }
    }

    record RemoveReactionAction(String reaction) implements AbilityAction {
        public RemoveReactionAction {
            if (reaction == null || reaction.isBlank()) {
                throw new IllegalArgumentException("Remove reaction reaction must not be blank");
            }
        }

        @Override
        public String type() {
            return "remove_reaction";
        }
    }

    record SpawnProjectileAction(String delivery,
                                 ActionTarget target,
                                 AbilityScalar speed,
                                 AbilityScalar inaccuracy) implements AbilityAction {
        public SpawnProjectileAction {
            if (delivery == null || delivery.isBlank()) {
                throw new IllegalArgumentException("Spawn projectile delivery must not be blank");
            }
            Objects.requireNonNull(target, "target");
            Objects.requireNonNull(speed, "speed");
            Objects.requireNonNull(inaccuracy, "inaccuracy");
        }

        @Override
        public String type() {
            return "spawn_projectile";
        }
    }
}
