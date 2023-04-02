package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.core.talents.MKTalent;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class AbilitySource {
    private static final String SEP_CHAR = "|";
    private static final String SEP_REGEX = "\\|";

    public static AbilitySource TRAINED = new AbilitySource(AbilitySourceType.TRAINED);
    public static AbilitySource GRANTED = new AbilitySource(AbilitySourceType.GRANTED);
    public static AbilitySource ADMIN = new AbilitySource(AbilitySourceType.ADMIN);

    public static AbilitySource forItem(ItemStack item) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item.getItem());
        return new ItemAbilitySource(id);
    }

    public static AbilitySource forTalent(MKTalent talent) {
        return new TalentSource(talent.getTalentId());
    }

    protected final AbilitySourceType sourceType;

    protected AbilitySource(AbilitySourceType type) {
        this.sourceType = type;
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
        return sourceType.name();
    }

    public static AbilitySource decode(AbilitySourceType type, String encoded) {
        return new AbilitySource(type);
    }

    public StringTag serialize() {
        return StringTag.valueOf(encode());
    }

    public static AbilitySource deserialize(String encoded) {
        String[] parts = encoded.split(SEP_REGEX, 2);

        if (parts.length > 0) {
            AbilitySourceType type = AbilitySourceType.valueOf(parts[0]);
            String remainder = parts.length > 1 ? parts[1] : "";
            return type.getFactory().apply(type, remainder);
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
        return sourceType == that.sourceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceType);
    }

    public static class ItemAbilitySource extends AbilitySource {

        private final ResourceLocation itemId;

        public ItemAbilitySource(ResourceLocation itemId) {
            super(AbilitySourceType.ITEM);
            this.itemId = itemId;
        }

        public String encode() {
            return super.encode() + SEP_CHAR + itemId.toString();
        }

        public static ItemAbilitySource decode(AbilitySourceType type, String typeSpecificData) {
            if (type != AbilitySourceType.ITEM) {
                return null;
            }

            ResourceLocation sourceId = ResourceLocation.tryParse(typeSpecificData);
            if (sourceId != null) {
                return new ItemAbilitySource(sourceId);
            }
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            ItemAbilitySource that = (ItemAbilitySource) o;
            return itemId.equals(that.itemId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), itemId);
        }
    }

    public static class TalentSource extends AbilitySource {
        private final ResourceLocation talentId;

        public TalentSource(ResourceLocation talentId) {
            super(AbilitySourceType.TALENT);
            this.talentId = talentId;
        }

        public String encode() {
            return super.encode() + SEP_CHAR + talentId.toString();
        }

        public static TalentSource decode(AbilitySourceType type, String typeSpecificData) {
            if (type != AbilitySourceType.TALENT) {
                return null;
            }

            ResourceLocation sourceId = ResourceLocation.tryParse(typeSpecificData);
            if (sourceId != null) {
                return new TalentSource(sourceId);
            }
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            TalentSource that = (TalentSource) o;
            return talentId.equals(that.talentId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), talentId);
        }
    }
}
