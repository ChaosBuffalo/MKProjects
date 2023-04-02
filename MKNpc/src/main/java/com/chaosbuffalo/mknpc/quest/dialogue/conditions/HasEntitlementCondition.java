package com.chaosbuffalo.mknpc.quest.dialogue.conditions;

import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.chaosbuffalo.mknpc.MKNpc;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;

public class HasEntitlementCondition extends DialogueCondition {
    public final static ResourceLocation conditionTypeName = new ResourceLocation(MKNpc.MODID, "has_entitlement");
    private MKEntitlement entitlement;

    public HasEntitlementCondition(MKEntitlement entitlement){
        super(conditionTypeName);
        this.entitlement = entitlement;
    }

    public HasEntitlementCondition(){
        super(conditionTypeName);
    }

    @Override
    public boolean meetsCondition(ServerPlayer serverPlayerEntity, LivingEntity livingEntity) {
        if (entitlement == null){
            return false;
        }
        return MKCore.getPlayer(serverPlayerEntity).map(x -> x.getEntitlements().hasEntitlement(entitlement)).orElse(false);
    }

    @Override
    public HasEntitlementCondition copy() {
        return new HasEntitlementCondition(entitlement);
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("entitlement"), ops.createString(entitlement.getId().toString()));
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        dynamic.get("entitlement").asString().result().ifPresent(
                x -> entitlement = MKCoreRegistry.getEntitlement(new ResourceLocation(x)));
    }
}
