package com.chaosbuffalo.mkcore.core.talents.talent_types;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.talents.MKTalent;
import com.chaosbuffalo.mkcore.core.talents.TalentNode;
import com.chaosbuffalo.mkcore.core.talents.TalentRecord;
import com.chaosbuffalo.mkcore.core.talents.TalentType;
import com.chaosbuffalo.mkcore.core.talents.nodes.AttributeTalentNode;
import com.mojang.serialization.Dynamic;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

public class AttributeTalent extends MKTalent {
    private final UUID id;
    private final Attribute attribute;
    private AttributeModifier.Operation operation;
    private boolean renderAsPercentage;
    private double defaultPerRank;
    private boolean requiresStatRefresh;

    public AttributeTalent(Attribute attr, UUID id) {
        this.id = id;
        this.attribute = attr;
        this.operation = AttributeModifier.Operation.ADDITION;
        this.renderAsPercentage = false;
        defaultPerRank = 1;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public UUID getUUID() {
        return id;
    }

    public AttributeModifier.Operation getOp() {
        return operation;
    }

    public AttributeTalent setOp(AttributeModifier.Operation value) {
        operation = value;
        return this;
    }

    public AttributeTalent setDisplayAsPercentage(boolean usePercentage) {
        renderAsPercentage = usePercentage;
        return this;
    }

    public AttributeTalent setDefaultPerRank(double valuePerRank) {
        defaultPerRank = valuePerRank;
        return this;
    }

    public AttributeTalent setRequiresStatRefresh(boolean needs) {
        requiresStatRefresh = needs;
        return this;
    }

    @Override
    public String toString() {
        return String.format("AttributeTalent[%s, %s, %s]", attribute.getDescriptionId(), id, operation);
    }

    private String getDescriptionTranslationKey() {
        ResourceLocation id = getTalentId();
        return String.format("%s.%s.description", id.getNamespace(), id.getPath());
    }

    @Override
    public Component getTalentDescription(TalentRecord record) {
        double perRank = 0;
        double currentValue = 0;
        if (record.getNode() instanceof AttributeTalentNode attrNode) {
            perRank = attrNode.getPerRank();
            currentValue = record.getRank() * perRank;
        } else {
            MKCore.LOGGER.error("Trying to create a tooltip for {} but the node was not an AttributeTalentNode!", this);
        }
        String amount;
        String totalAmount;
        if (renderAsPercentage) {
            amount = String.format("%.2f%%", perRank * 100);
            totalAmount = String.format("%.2f%%", currentValue * 100);
        } else {
            amount = String.format("%.2f", perRank);
            totalAmount = String.format("%.2f", currentValue);
        }
        String finalAmount = String.format("%s (%s)", amount, totalAmount);
        return new TranslatableComponent(getDescriptionTranslationKey(), finalAmount).withStyle(ChatFormatting.GRAY);
    }

    public AttributeModifier createModifier(double value) {
        return new AttributeModifier(getUUID(), () -> getTalentId().toString(), value, getOp());
    }

    public double getDefaultPerRank() {
        return defaultPerRank;
    }

    public boolean requiresStatRefresh() {
        return requiresStatRefresh;
    }

    @Override
    public TalentType<?> getTalentType() {
        return TalentType.ATTRIBUTE;
    }

    @Override
    public <T> TalentNode createNode(Dynamic<T> dynamic) {
        return new AttributeTalentNode(this, dynamic);
    }
}
