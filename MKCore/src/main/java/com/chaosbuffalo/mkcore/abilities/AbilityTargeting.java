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

    static AbilityContext noTarget(IMKEntityData entityData, MKAbilityInfo abilityInfo) {
        return new AbilityContext(entityData, abilityInfo);
    }

    private static AbilityContext selectSelf(IMKEntityData entityData, MKAbilityInfo abilityInfo) {
        MKCore.LOGGER.debug("AbilityTargeting.SELF {} {}", abilityInfo.getId(), entityData.getEntity());
        return AbilityContext.selfTarget(entityData, abilityInfo);
    }

    private static AbilityContext selectSingle(IMKEntityData entityData, MKAbilityInfo abilityInfo) {
        MKAbility ability = abilityInfo.getAbility();
        LivingEntity targetEntity = TargetUtil.getSingleLivingTarget(entityData.getEntity(),
                ability.getDistance(entityData.getEntity()),
                ability::isValidTarget);
        MKCore.LOGGER.debug("AbilityTargeting.SINGLE_TARGET {} {} {}", abilityInfo.getId(), entityData.getEntity(), targetEntity);
        return AbilityContext.singleTarget(entityData, targetEntity, abilityInfo);
    }

    private static AbilityContext selectSingleOrSelf(IMKEntityData entityData, MKAbilityInfo abilityInfo) {
        MKAbility ability = abilityInfo.getAbility();
        LivingEntity targetEntity = TargetUtil.getSingleLivingTargetOrSelf(entityData.getEntity(),
                ability.getDistance(entityData.getEntity()),
                ability::isValidTarget);
        MKCore.LOGGER.debug("AbilityTargeting.SINGLE_TARGET_OR_SELF {} {} {}", abilityInfo.getId(),
                entityData.getEntity(), targetEntity);
        return AbilityContext.singleTarget(entityData, targetEntity, abilityInfo);
    }

    private static AbilityContext selectPositionIncludeEntities(IMKEntityData entityData, MKAbilityInfo abilityInfo) {
        MKAbility ability = abilityInfo.getAbility();
        TargetUtil.LivingOrPosition targetPos = TargetUtil.getPositionTarget(entityData.getEntity(), ability.getDistance(entityData.getEntity()),
                ability::isValidTarget);
        MKCore.LOGGER.debug("AbilityTargeting.POSITION_INCLUDE_ENTITIES {} {} {}", abilityInfo.getId(),
                entityData.getEntity(), targetPos != null ? targetPos : "EMPTY");
        return AbilityContext.singleOrPositionTarget(entityData, abilityInfo, targetPos);
    }
}
