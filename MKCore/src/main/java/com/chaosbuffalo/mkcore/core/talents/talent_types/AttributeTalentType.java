package com.chaosbuffalo.mkcore.core.talents.talent_types;

import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.core.talents.TalentRecord;
import com.chaosbuffalo.mkcore.core.talents.TalentType;
import com.chaosbuffalo.mkcore.core.talents.TalentTypeHandler;
import com.chaosbuffalo.mkcore.core.talents.handlers.AttributeTalentHandler;
import com.chaosbuffalo.mkcore.core.talents.nodes.AttributeTalentNode;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;

import java.util.Objects;

public class AttributeTalentType extends TalentType<AttributeTalentNode> {

    public static String nameKey(ResourceLocation attrId) {
        return attrId.toLanguageKey("attribute_talent", "name");
    }

    public static String descriptionKey(ResourceLocation attrId) {
        return attrId.toLanguageKey("attribute_talent", "description");
    }

    @Override
    public TalentTypeHandler createTypeHandler(Persona persona) {
        return new AttributeTalentHandler(persona);
    }

    @Override
    public MapCodec<AttributeTalentNode> codec() {
        return AttributeTalentNode.MAP_CODEC;
    }

    @Override
    public MutableComponent getTalentNodeName(TalentRecord record) {
        if (!(record.getNode() instanceof AttributeTalentNode attrNode)) {
            return Component.literal("bad talent type");
        }

        return Component.translatable(attrNode.getAttribute().value().getDescriptionId());
    }

    @Override
    public MutableComponent getTypeDisplayName(TalentRecord record) {
        return Component.translatableWithFallback("talent_type.mkcore.attribute.name", "Attribute Bonus");
    }

    @Override
    public MutableComponent getTalentDescription(TalentRecord record) {
        if (!(record.getNode() instanceof AttributeTalentNode attrNode)) {
            return Component.literal("bad talent type");
        }

        double perRank = attrNode.getPerRank();
        double currentValue = record.getRank() * perRank;
        var amount = attrNode.getAttribute().value().toValueComponent(attrNode.getOperation(), perRank, TooltipFlag.NORMAL);
        var totalAmount = attrNode.getAttribute().value().toValueComponent(attrNode.getOperation(), currentValue, TooltipFlag.NORMAL);
        Component finalAmount = Component.empty().append(amount).append(" (").append(totalAmount).append(")");

        var attributeId = attrNode.getAttribute().getKey();
        Objects.requireNonNull(attributeId);
        return Component.translatable(descriptionKey(attributeId.location()), finalAmount);
    }
}
