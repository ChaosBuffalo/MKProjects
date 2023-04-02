package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.description.AbilityDescriptions;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.utils.TargetUtil;
import com.google.common.collect.ImmutableSet;
import net.minecraft.world.entity.LivingEntity;

public class AbilityTargeting {

    public static final AbilityTargetSelector NONE = new AbilityTargetSelector(AbilityTargeting::noTarget)
            .setDescriptionKey("mkcore.ability_target.none");

    public static final AbilityTargetSelector PROJECTILE = new AbilityTargetSelector(AbilityTargeting::noTarget)
            .setDescriptionKey("mkcore.ability_target.projectile");

    public static final AbilityTargetSelector LINE = new AbilityTargetSelector(AbilityTargeting::noTarget)
            .addDynamicDescription(AbilityDescriptions::getRangeDescription)
            .setDescriptionKey("mkcore.ability_target.line");

    public static final AbilityTargetSelector SELF = new AbilityTargetSelector(AbilityTargeting::selectSelf)
            .setRequiredMemories(ImmutableSet.of(MKAbilityMemories.ABILITY_TARGET.get()))
            .setShowTargetType(false)
            .setDescriptionKey("mkcore.ability_target.self");

    public static final AbilityTargetSelector SINGLE_TARGET = new AbilityTargetSelector(AbilityTargeting::selectSingle)
            .setRequiredMemories(ImmutableSet.of(MKAbilityMemories.ABILITY_TARGET.get()))
            .addDynamicDescription(AbilityDescriptions::getRangeDescription)
            .setDescriptionKey("mkcore.ability_target.single_target");

    public static final AbilityTargetSelector SINGLE_TARGET_OR_SELF = new AbilityTargetSelector(AbilityTargeting::selectSingleOrSelf)
            .setRequiredMemories(ImmutableSet.of(MKAbilityMemories.ABILITY_TARGET.get()))
            .addDynamicDescription(AbilityDescriptions::getRangeDescription)
            .setDescriptionKey("mkcore.ability_target.single_target_self");

    public static final AbilityTargetSelector PBAOE = new AbilityTargetSelector(AbilityTargeting::noTarget)
            .setDescriptionKey("mkcore.ability_target.pbaoe")
            .addDynamicDescription(AbilityDescriptions::getRangeDescription);

    public static final AbilityTargetSelector POSITION_INCLUDE_ENTITIES = new AbilityTargetSelector(AbilityTargeting::selectPositionIncludeEntities)
            .setRequiredMemories(ImmutableSet.of(MKAbilityMemories.ABILITY_POSITION_TARGET.get()))
            .setDescriptionKey("mkcore.ability_target.position_include_entities")
            .addDynamicDescription(AbilityDescriptions::getRangeDescription);

    static AbilityContext noTarget(IMKEntityData entityData, MKAbility ability) {
        return AbilityContext.EMPTY;
    }

    private static AbilityContext selectSelf(IMKEntityData entityData, MKAbility ability) {
        MKCore.LOGGER.debug("AbilityTargeting.SELF {} {}", ability.getAbilityId(), entityData.getEntity());
        return AbilityContext.selfTarget(entityData);
    }

    private static AbilityContext selectSingle(IMKEntityData entityData, MKAbility ability) {
        LivingEntity targetEntity = TargetUtil.getSingleLivingTarget(entityData.getEntity(),
                ability.getDistance(entityData.getEntity()),
                ability::isValidTarget);
        MKCore.LOGGER.debug("AbilityTargeting.SINGLE_TARGET {} {} {}", ability.getAbilityId(), entityData.getEntity(), targetEntity);
        return AbilityContext.singleTarget(targetEntity);
    }

    private static AbilityContext selectSingleOrSelf(IMKEntityData entityData, MKAbility ability) {
        LivingEntity targetEntity = TargetUtil.getSingleLivingTargetOrSelf(entityData.getEntity(),
                ability.getDistance(entityData.getEntity()),
                ability::isValidTarget);
        MKCore.LOGGER.debug("AbilityTargeting.SINGLE_TARGET_OR_SELF {} {} {}", ability.getAbilityId(),
                entityData.getEntity(), targetEntity);
        return AbilityContext.singleTarget(targetEntity);
    }

    private static AbilityContext selectPositionIncludeEntities(IMKEntityData entityData, MKAbility ability) {
        TargetUtil.LivingOrPosition targetPos = TargetUtil.getPositionTarget(entityData.getEntity(), ability.getDistance(entityData.getEntity()),
                ability::isValidTarget);
        MKCore.LOGGER.debug("AbilityTargeting.POSITION_INCLUDE_ENTITIES {} {} {}", ability.getAbilityId(),
                entityData.getEntity(), targetPos != null ? targetPos : "EMPTY");
        return AbilityContext.singleOrPositionTarget(targetPos);
    }
}
