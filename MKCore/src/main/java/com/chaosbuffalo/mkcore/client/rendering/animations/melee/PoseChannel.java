package com.chaosbuffalo.mkcore.client.rendering.animations.melee;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record PoseChannel(String target, Property property, Operation operation, Sign sign, PoseValue value) {
    public static final Codec<PoseChannel> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("target").forGetter(PoseChannel::target),
            Property.CODEC.fieldOf("property").forGetter(PoseChannel::property),
            Operation.CODEC.optionalFieldOf("operation", Operation.SET).forGetter(PoseChannel::operation),
            Sign.CODEC.optionalFieldOf("sign", Sign.NONE).forGetter(PoseChannel::sign),
            PoseValue.CODEC.fieldOf("value").forGetter(PoseChannel::value)
    ).apply(instance, PoseChannel::new));

    public enum Operation {
        SET("set"),
        ADD("add"),
        MAX("max");

        public static final Codec<Operation> CODEC = Codec.STRING.xmap(Operation::fromJson, Operation::getJsonName);

        private final String jsonName;

        Operation(String jsonName) {
            this.jsonName = jsonName;
        }

        public static Operation fromJson(String value) {
            return Operation.valueOf(value.toUpperCase());
        }

        public String getJsonName() {
            return jsonName;
        }
    }

    public enum Property {
        X_ROT("xRot"),
        Y_ROT("yRot"),
        Z_ROT("zRot"),
        X("x"),
        Y("y"),
        Z("z");

        private final String jsonName;

        public static final Codec<Property> CODEC = Codec.STRING.xmap(Property::fromJson, Property::getJsonName);

        Property(String jsonName) {
            this.jsonName = jsonName;
        }

        public static Property fromJson(String value) {
            for (Property property : values()) {
                if (property.jsonName.equals(value)) {
                    return property;
                }
            }
            throw new IllegalArgumentException("Unknown melee animation property: " + value);
        }

        public String getJsonName() {
            return jsonName;
        }
    }

    public enum Sign {
        NONE("none"),
        NEGATED_HANDEDNESS_TIMES_SWING_DIRECTION("-handedness*swingDirection"),
        HANDEDNESS_TIMES_SWING_DIRECTION("handedness*swingDirection"),
        HANDEDNESS("handedness"),
        NEGATED_HANDEDNESS("-handedness"),
        SWING_DIRECTION("swingDirection"),
        NEGATED_SWING_DIRECTION("-swingDirection");

        private final String jsonName;

        public static final Codec<Sign> CODEC = Codec.STRING.xmap(Sign::fromJson, Sign::getJsonName);

        Sign(String jsonName) {
            this.jsonName = jsonName;
        }

        public static Sign fromJson(String value) {
            for (Sign sign : values()) {
                if (sign.jsonName.equals(value)) {
                    return sign;
                }
            }
            throw new IllegalArgumentException("Unknown melee animation sign: " + value);
        }

        public String getJsonName() {
            return jsonName;
        }
    }

    public enum Curve {
        NONE(""),
        SWING_INVERSE("swing_inverse"),
        SWING_SIN("swing_sin"),
        IMPACT("impact"),
        FOLLOW_THROUGH("follow_through"),
        FOLLOW_THROUGH_SHORT("follow_through_short"),
        WINDUP_SIN("windup_sin"),
        AGE_SIN("age_sin");

        private final String jsonName;

        public static final Codec<Curve> CODEC = Codec.STRING.xmap(Curve::fromJson, Curve::getJsonName);

        Curve(String jsonName) {
            this.jsonName = jsonName;
        }

        public static Curve fromJson(String value) {
            for (Curve curve : values()) {
                if (curve.jsonName.equals(value)) {
                    return curve;
                }
            }
            throw new IllegalArgumentException("Unknown melee animation curve: " + value);
        }

        public String getJsonName() {
            return jsonName;
        }
    }

    public enum Input {
        NONE(""),
        HEAD_PITCH("headPitch"),
        NET_HEAD_YAW("netHeadYaw");

        private final String jsonName;

        public static final Codec<Input> CODEC = Codec.STRING.xmap(Input::fromJson, Input::getJsonName);

        Input(String jsonName) {
            this.jsonName = jsonName;
        }

        public static Input fromJson(String value) {
            for (Input input : values()) {
                if (input.jsonName.equals(value)) {
                    return input;
                }
            }
            throw new IllegalArgumentException("Unknown melee animation input: " + value);
        }

        public String getJsonName() {
            return jsonName;
        }
    }

    public record PoseValue(float constant, List<PoseTerm> terms) {
        public static final Codec<PoseValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.optionalFieldOf("constant", 0.0F).forGetter(PoseValue::constant),
                PoseTerm.CODEC.listOf().optionalFieldOf("terms", List.of()).forGetter(PoseValue::terms)
        ).apply(instance, PoseValue::new));
    }

    public record PoseTerm(Curve curve, Input input, Curve multiplierCurve, float scale, float frequency,
                           float offset, boolean absolute) {
        public static final Codec<PoseTerm> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Curve.CODEC.optionalFieldOf("curve", Curve.NONE).forGetter(PoseTerm::curve),
                Input.CODEC.optionalFieldOf("input", Input.NONE).forGetter(PoseTerm::input),
                Curve.CODEC.optionalFieldOf("multiplierCurve", Curve.NONE).forGetter(PoseTerm::multiplierCurve),
                Codec.FLOAT.optionalFieldOf("scale", 1.0F).forGetter(PoseTerm::scale),
                Codec.FLOAT.optionalFieldOf("frequency", 1.0F).forGetter(PoseTerm::frequency),
                Codec.FLOAT.optionalFieldOf("offset", 0.0F).forGetter(PoseTerm::offset),
                Codec.BOOL.optionalFieldOf("absolute", false).forGetter(PoseTerm::absolute)
        ).apply(instance, PoseTerm::new));
    }
}
