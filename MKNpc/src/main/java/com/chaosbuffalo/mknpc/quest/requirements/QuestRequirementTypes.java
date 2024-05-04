package com.chaosbuffalo.mknpc.quest.requirements;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.quest.QuestRegistries;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class QuestRequirementTypes {
    public static final DeferredRegister<QuestRequirementType<?>> REGISTRY = DeferredRegister.create(QuestRegistries.QUEST_REQUIREMENT_TYPES_REGISTRY_NAME, MKNpc.MODID);

    public static final Supplier<QuestRequirementType<HasEntitlementRequirement>> HAS_ENTITLEMENT = REGISTRY.register("has_entitlement", () -> () -> HasEntitlementRequirement.CODEC);
}
