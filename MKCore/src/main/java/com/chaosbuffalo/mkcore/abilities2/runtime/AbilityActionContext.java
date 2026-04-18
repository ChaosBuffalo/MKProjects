package com.chaosbuffalo.mkcore.abilities2.runtime;

import com.chaosbuffalo.mkcore.abilities2.definition.AbilityValue;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public interface AbilityActionContext {
    AbilityInvocation invocation();

    IMKEntityData ownerData();

    IMKEntityData casterData();

    UUID sourceId();

    AbilityResolvedTargets targets();

    Optional<LivingEntity> currentTarget();

    @Nullable
    AbilityEventSnapshot eventSnapshot();

    Optional<AbilityReactionOwner> reactionOwner();

    AbilityValue getParam(String id);

    AbilityValue getVar(String id);

    void setVar(String id, AbilityValue value);

    default float getFloatParam(String id) {
        return getParam(id).asFloat(id);
    }

    default int getIntParam(String id) {
        return getParam(id).asInt(id);
    }

    default boolean getBoolParam(String id) {
        return getParam(id).asBool(id);
    }

    default String getStringParam(String id) {
        return getParam(id).asString(id);
    }

    default ResourceLocation getResourceLocationParam(String id) {
        return getParam(id).asResourceLocation(id);
    }

    default float getFloatVar(String id) {
        return getVar(id).asFloat(id);
    }

    default int getIntVar(String id) {
        return getVar(id).asInt(id);
    }

    default boolean getBoolVar(String id) {
        return getVar(id).asBool(id);
    }

    default String getStringVar(String id) {
        return getVar(id).asString(id);
    }

    default ResourceLocation getResourceLocationVar(String id) {
        return getVar(id).asResourceLocation(id);
    }

    AbilityStatSnapshot stats(StatCapturePolicy policy);

    AbilityStateStore stateStore();
}
