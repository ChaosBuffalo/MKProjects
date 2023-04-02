package com.chaosbuffalo.mknpc.quest.requirements;

import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.quest.dialogue.conditions.HasEntitlementCondition;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;

public class HasEntitlementRequirement extends QuestRequirement{
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKNpc.MODID, "quest_requirement.has_entitlement");
    private MKEntitlement entitlement;

    public HasEntitlementRequirement(MKEntitlement entitlement){
        super(TYPE_NAME);
        this.entitlement = entitlement;
    }

    public HasEntitlementRequirement(){
        super(TYPE_NAME);
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

    @Override
    public boolean meetsRequirements(Player player) {
        if (entitlement == null){
            return false;
        }
        return MKCore.getPlayer(player).map(x -> x.getEntitlements().hasEntitlement(entitlement)).orElse(false);
    }

    @Override
    public DialogueCondition getDialogueCondition() {
        return new HasEntitlementCondition(entitlement);
    }
}
