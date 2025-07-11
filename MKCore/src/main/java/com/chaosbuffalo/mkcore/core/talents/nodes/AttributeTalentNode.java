package com.chaosbuffalo.mkcore.core.talents.nodes;


import com.chaosbuffalo.mkcore.core.talents.*;
import com.chaosbuffalo.mkcore.init.CoreTalentTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class AttributeTalentNode extends TalentNode {
    public static final MapCodec<AttributeTalentNode> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(AttributeTalentNode::getAttribute),
            TalentNodeDisplay.REFERENCE_CODEC.fieldOf("display_info").forGetter(i -> i.displayHolder),
            Codec.INT.fieldOf("max_ranks").forGetter(TalentNode::getMaxRanks),
            Codec.DOUBLE.fieldOf("per_rank").forGetter(AttributeTalentNode::getPerRank),
            AttributeModifier.Operation.CODEC.optionalFieldOf("operation", AttributeModifier.Operation.ADD_VALUE).forGetter(AttributeTalentNode::getOperation)
    ).apply(builder, AttributeTalentNode::new));

    private final double perRank;
    private final AttributeModifier.Operation operation;
    private final Holder<Attribute> attribute;

    public AttributeTalentNode(Holder<Attribute> attribute, Holder<TalentNodeDisplay> displayHolder, int maxRanks, double perRank) {
        this(attribute, displayHolder, maxRanks, perRank, AttributeModifier.Operation.ADD_VALUE);
    }

    public AttributeTalentNode(Holder<Attribute> attribute, Holder<TalentNodeDisplay> displayHolder, int maxRanks, double perRank, AttributeModifier.Operation operation) {
        super(displayHolder, maxRanks);
        this.perRank = perRank;
        this.attribute = attribute;
        this.operation = operation;
    }

    @Override
    public TalentType<AttributeTalentNode> getType() {
        return CoreTalentTypes.ATTRIBUTE.get();
    }

    public double getValue(int rank) {
        return perRank * rank;
    }

    public double getPerRank() {
        return perRank;
    }

    public Holder<Attribute> getAttribute() {
        return attribute;
    }

    public AttributeModifier.Operation getOperation() {
        return operation;
    }

    public AttributeModifier createModifier(TalentRecord record) {
        ResourceLocation modId = record.getUniqueId().withSuffix("/%d".formatted(operation.ordinal()));

        double value = getValue(record.getRank());
        return new AttributeModifier(modId, value, operation);
    }
}
