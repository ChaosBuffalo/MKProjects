package com.chaosbuffalo.mknpc.quest.requirements;

import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.chaosbuffalo.mknpc.quest.dialogue.conditions.HasEntitlementCondition;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;

public class HasEntitlementRequirement extends QuestRequirement {
    public static final Codec<HasEntitlementRequirement> CODEC = ExtraCodecs.lazyInitializedCodec(() ->
            RecordCodecBuilder.<HasEntitlementRequirement>mapCodec(builder ->
                    builder.group(
                            MKCoreRegistry.ENTITLEMENTS.getCodec().fieldOf("entitlement").forGetter(i -> i.entitlement)
                    ).apply(builder, HasEntitlementRequirement::new)
            ).codec());


    private final MKEntitlement entitlement;

    public HasEntitlementRequirement(MKEntitlement entitlement) {
        this.entitlement = entitlement;
    }

    @Override
    public QuestRequirementType<? extends QuestRequirement> getType() {
        return QuestRequirementTypes.HAS_ENTITLEMENT.get();
    }

    @Override
    public boolean meetsRequirements(Player player) {
        if (entitlement == null) {
            return false;
        }
        return MKCore.getPlayer(player).map(x -> x.getEntitlements().hasEntitlement(entitlement)).orElse(false);
    }

    @Override
    public DialogueCondition getDialogueCondition() {
        return new HasEntitlementCondition(entitlement);
    }
}
