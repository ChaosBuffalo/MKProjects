package com.chaosbuffalo.mkcore.core.talents.nodes;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.talents.TalentNode;
import com.chaosbuffalo.mkcore.core.talents.TalentNodeDisplay;
import com.chaosbuffalo.mkcore.core.talents.TalentType;
import com.chaosbuffalo.mkcore.init.CoreTalentTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;

public class AbilityGrantTalentNode extends TalentNode {

    public static final MapCodec<AbilityGrantTalentNode> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            MKCoreRegistry.ABILITIES.holderByNameCodec().fieldOf("ability").forGetter(i -> i.ability),
            TalentNodeDisplay.REFERENCE_CODEC.fieldOf("display_info").forGetter(i -> i.displayHolder)
    ).apply(builder, AbilityGrantTalentNode::new));

    private final Holder<MKAbility> ability;

    public AbilityGrantTalentNode(Holder<MKAbility> ability, Holder<TalentNodeDisplay> displayHolder) {
        super(displayHolder, 1);
        this.ability = ability;
    }

    @Override
    public TalentType<AbilityGrantTalentNode> getType() {
        return CoreTalentTypes.ABILITY_GRANT.get();
    }

    public MKAbility getAbility() {
        return ability.value();
    }
}
