package com.chaosbuffalo.mkcore.core.talents.nodes;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityDefinitionData;
import com.chaosbuffalo.mkcore.core.talents.TalentNode;
import com.chaosbuffalo.mkcore.core.talents.TalentNodeDisplay;
import com.chaosbuffalo.mkcore.core.talents.TalentType;
import com.chaosbuffalo.mkcore.init.CoreTalentTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class AbilityGrantTalentNode extends TalentNode {

    public static final MapCodec<AbilityGrantTalentNode> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("ability").forGetter(AbilityGrantTalentNode::getAbilityId),
            TalentNodeDisplay.REFERENCE_CODEC.fieldOf("display_info").forGetter(i -> i.displayHolder)
    ).apply(builder, AbilityGrantTalentNode::new));

    private final ResourceLocation abilityId;

    public AbilityGrantTalentNode(ResourceLocation abilityId, Holder<TalentNodeDisplay> displayHolder) {
        super(displayHolder, 1);
        this.abilityId = abilityId;
    }

    public AbilityGrantTalentNode(Holder<MKAbility> ability, Holder<TalentNodeDisplay> displayHolder) {
        this(ability.value().getAbilityId(), displayHolder);
    }

    @Override
    public TalentType<AbilityGrantTalentNode> getType() {
        return CoreTalentTypes.ABILITY_GRANT.get();
    }

    public ResourceLocation getAbilityId() {
        return abilityId;
    }

    public @Nullable MKAbility getAbility() {
        return MKCoreRegistry.getAbility(abilityId);
    }

    public @Nullable AbilityDefinitionData getAbilityDefinition() {
        return MKCore.getAbilityDefinitionService().getDefinition(abilityId);
    }
}
