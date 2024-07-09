package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.talents.TalentNode;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EquipmentSlot;

import javax.annotation.Nullable;
import java.util.Objects;

public class AbilitySource {
    private static final String SEP_CHAR = "|";
    private static final String SEP_REGEX = "\\|";

    public static AbilitySource TRAINED = new AbilitySource(AbilitySourceType.TRAINED);
    public static AbilitySource GRANTED = new AbilitySource(AbilitySourceType.GRANTED);
    public static AbilitySource ADMIN = new AbilitySource(AbilitySourceType.ADMIN);

    public static AbilitySource forEquipmentSlot(EquipmentSlot slot) {
        return new AbilitySource(AbilitySourceType.ITEM, slot.name());
    }

    public static AbilitySource forTalent(TalentNode node) {
        String uniqueId = node.getPositionString();
        return new AbilitySource(AbilitySourceType.TALENT, uniqueId);
    }

    protected final AbilitySourceType sourceType;
    @Nullable
    protected final String uniqueData;

    protected AbilitySource(AbilitySourceType type) {
        this(type, null);
    }

    protected AbilitySource(AbilitySourceType type, @Nullable String unique) {
        sourceType = type;
        uniqueData = unique;
    }

    public AbilitySourceType getSourceType() {
        return sourceType;
    }

    public boolean placeOnBarWhenLearned() {
        return sourceType.placeOnBarWhenLearned();
    }

    public boolean usesAbilityPool() {
        return sourceType.usesAbilityPool();
    }

    public String encode() {
        if (uniqueData != null) {
            return sourceType.name() + SEP_CHAR + uniqueData;
        }
        return sourceType.name();
    }

    public static AbilitySource decode(AbilitySourceType type, String extra) {
        return new AbilitySource(type, extra);
    }

    public Tag serialize() {
        return StringTag.valueOf(encode());
    }

    public static AbilitySource deserialize(String encoded) {
        String[] parts = encoded.split(SEP_REGEX, 2);

        if (parts.length > 0) {
            try {
                AbilitySourceType type = AbilitySourceType.valueOf(parts[0]);
                String remainder = parts.length > 1 ? parts[1] : null;
                return new AbilitySource(type, remainder);
            } catch (IllegalArgumentException e) {
                MKCore.LOGGER.error("Failed to decode AbilitySource '{}'", encoded);
                return null;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return "AbilitySource{" + encode() + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbilitySource that = (AbilitySource) o;
        return sourceType == that.sourceType && Objects.equals(uniqueData, that.uniqueData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceType, uniqueData);
    }
}
