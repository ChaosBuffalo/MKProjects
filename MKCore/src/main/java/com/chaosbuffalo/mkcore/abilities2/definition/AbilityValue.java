package com.chaosbuffalo.mkcore.abilities2.definition;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.UUID;

public sealed interface AbilityValue permits AbilityValue.FloatValue, AbilityValue.IntValue, AbilityValue.BoolValue,
        AbilityValue.StringValue, AbilityValue.EntityRefValue, AbilityValue.ResourceLocationValue {

    AbilityValueKind kind();

    default float asFloat(String id) {
        throw typeError(id, AbilityValueKind.FLOAT);
    }

    default int asInt(String id) {
        throw typeError(id, AbilityValueKind.INT);
    }

    default boolean asBool(String id) {
        throw typeError(id, AbilityValueKind.BOOL);
    }

    default String asString(String id) {
        throw typeError(id, AbilityValueKind.STRING);
    }

    default UUID asEntityRef(String id) {
        throw typeError(id, AbilityValueKind.ENTITY_REF);
    }

    default ResourceLocation asResourceLocation(String id) {
        throw typeError(id, AbilityValueKind.RESOURCE_LOCATION);
    }

    private IllegalStateException typeError(String id, AbilityValueKind expectedKind) {
        return new IllegalStateException("Ability value '%s' expected %s but was %s"
                .formatted(id, expectedKind, kind()));
    }

    record FloatValue(float value) implements AbilityValue {
        @Override
        public AbilityValueKind kind() {
            return AbilityValueKind.FLOAT;
        }

        @Override
        public float asFloat(String id) {
            return value;
        }
    }

    record IntValue(int value) implements AbilityValue {
        @Override
        public AbilityValueKind kind() {
            return AbilityValueKind.INT;
        }

        @Override
        public int asInt(String id) {
            return value;
        }
    }

    record BoolValue(boolean value) implements AbilityValue {
        @Override
        public AbilityValueKind kind() {
            return AbilityValueKind.BOOL;
        }

        @Override
        public boolean asBool(String id) {
            return value;
        }
    }

    record StringValue(String value) implements AbilityValue {
        public StringValue {
            Objects.requireNonNull(value, "value");
        }

        @Override
        public AbilityValueKind kind() {
            return AbilityValueKind.STRING;
        }

        @Override
        public String asString(String id) {
            return value;
        }
    }

    record EntityRefValue(UUID value) implements AbilityValue {
        public EntityRefValue {
            Objects.requireNonNull(value, "value");
        }

        @Override
        public AbilityValueKind kind() {
            return AbilityValueKind.ENTITY_REF;
        }

        @Override
        public UUID asEntityRef(String id) {
            return value;
        }
    }

    record ResourceLocationValue(ResourceLocation value) implements AbilityValue {
        public ResourceLocationValue {
            Objects.requireNonNull(value, "value");
        }

        @Override
        public AbilityValueKind kind() {
            return AbilityValueKind.RESOURCE_LOCATION;
        }

        @Override
        public ResourceLocation asResourceLocation(String id) {
            return value;
        }
    }
}
