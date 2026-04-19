package com.chaosbuffalo.mkcore.abilities.training.requirements;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.training.AbilityTrainingRequirement;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class HasEntitlementRequirement extends AbilityTrainingRequirement {
    public static final ResourceLocation TYPE_NAME = MKCore.id("training_req.has_entitlement");
    public static final MapCodec<HasEntitlementRequirement> CODEC = MKEntitlement.REFERENCE_CODEC
            .fieldOf("entitlement").xmap(HasEntitlementRequirement::new, i -> i.entitlement);

    private final Holder<MKEntitlement> entitlement;

    public HasEntitlementRequirement(Holder<MKEntitlement> entitlement) {
        super(TYPE_NAME);
        this.entitlement = entitlement;
    }

    @Override
    public boolean check(MKPlayerData playerData, ResourceLocation abilityId) {
        return playerData.getEntitlements().hasEntitlement(entitlement);
    }

    @Override
    public MutableComponent describe(MKPlayerData playerData) {
        return Component.literal("You must have earned: ")
                .append(entitlement.value().getName());
    }
}
