package com.chaosbuffalo.mkcore.abilities.training.requirements;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.abilities.training.AbilityTrainingRequirement;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public class HasEntitlementRequirement extends AbilityTrainingRequirement {
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKCore.MOD_ID, "training_req.has_entitlement");
    public static final Codec<HasEntitlementRequirement> CODEC = ExtraCodecs.lazyInitializedCodec(() -> MKCoreRegistry.ENTITLEMENTS.getCodec()).xmap(HasEntitlementRequirement::new, i -> i.entitlement);

    private final MKEntitlement entitlement;

    public HasEntitlementRequirement(MKEntitlement entitlement) {
        super(TYPE_NAME);
        this.entitlement = entitlement;
    }

    @Override
    public boolean check(MKPlayerData playerData, MKAbilityInfo abilityInfo) {
        return playerData.getEntitlements().hasEntitlement(entitlement);
    }

    @Override
    public void onLearned(MKPlayerData playerData, MKAbilityInfo abilityInfo) {

    }

    @Override
    public MutableComponent describe(MKPlayerData playerData) {
        return Component.literal("You must have earned: ")
                .append(entitlement.getDescription());
    }
}
