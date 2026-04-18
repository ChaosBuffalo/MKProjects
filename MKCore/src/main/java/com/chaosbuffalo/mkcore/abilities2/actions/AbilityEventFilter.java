package com.chaosbuffalo.mkcore.abilities2.actions;

import com.chaosbuffalo.mkcore.abilities2.definition.AbilityValue;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public sealed interface AbilityEventFilter permits AbilityEventFilter.EventParticipantFilter,
        AbilityEventFilter.EventSourceTagFilter, AbilityEventFilter.EventPayloadComparisonFilter,
        AbilityEventFilter.EventPayloadTagFilter {

    enum EventParticipant {
        ACTOR,
        TARGET
    }

    enum ParticipantRelation {
        IS_SELF,
        IS_ALLY,
        IS_ENEMY
    }

    enum ComparisonOp {
        EQ,
        NEQ,
        GT,
        GTE,
        LT,
        LTE;

        public boolean isNumericOnly() {
            return this == GT || this == GTE || this == LT || this == LTE;
        }
    }

    record EventParticipantFilter(EventParticipant participant,
                                  ParticipantRelation relation) implements AbilityEventFilter {
        public EventParticipantFilter {
            Objects.requireNonNull(participant, "participant");
            Objects.requireNonNull(relation, "relation");
        }
    }

    record EventSourceTagFilter(ResourceLocation tag) implements AbilityEventFilter {
        public EventSourceTagFilter {
            Objects.requireNonNull(tag, "tag");
        }
    }

    record EventPayloadComparisonFilter(String key,
                                        ComparisonOp op,
                                        AbilityValue value) implements AbilityEventFilter {
        public EventPayloadComparisonFilter {
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("Event payload comparison key must not be blank");
            }
            Objects.requireNonNull(op, "op");
            Objects.requireNonNull(value, "value");
            if (op.isNumericOnly() && !value.kind().isNumeric()) {
                throw new IllegalArgumentException("Comparison op %s requires a numeric AbilityValue, got %s"
                        .formatted(op, value.kind()));
            }
        }
    }

    record EventPayloadTagFilter(String key,
                                 ResourceLocation tag) implements AbilityEventFilter {
        public EventPayloadTagFilter {
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("Event payload tag key must not be blank");
            }
            Objects.requireNonNull(tag, "tag");
        }
    }
}
