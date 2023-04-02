package com.chaosbuffalo.mkcore.abilities.training.requirements;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.training.AbilityTrainingRequirement;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.NoSuchElementException;

public class HasEntitlementRequirement extends AbilityTrainingRequirement {
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKCore.MOD_ID, "training_req.has_entitlement");
    private MKEntitlement entitlement;

    public HasEntitlementRequirement(MKEntitlement entitlement) {
        super(TYPE_NAME);
        this.entitlement = entitlement;
    }

    public <D> HasEntitlementRequirement(Dynamic<D> dynamic) {
        super(TYPE_NAME, dynamic);
    }

    @Override
    public boolean check(MKPlayerData playerData, MKAbility ability) {
        return playerData.getEntitlements().hasEntitlement(entitlement);
    }

    @Override
    public void onLearned(MKPlayerData playerData, MKAbility ability) {

    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("entitlement"), ops.createString(entitlement.getId().toString()));
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        ResourceLocation entitlementId = dynamic.get("entitlement").asString()
                .resultOrPartial(error -> {
                    throw new IllegalArgumentException(String.format("Failed to parse entitlement requirement: %s", error));
                })
                .map(ResourceLocation::new)
                .orElseThrow(IllegalStateException::new);
        entitlement = MKCoreRegistry.getEntitlement(entitlementId);
        if (entitlement == null) {
            throw new NoSuchElementException(String.format("The entitlement '%s' does not exist", entitlementId));
        }
    }

    @Override
    public MutableComponent describe(MKPlayerData playerData) {
        return new TextComponent("You must have earned: ")
                .append(entitlement.getDescription());
    }
}
