package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.core.AbilityType;
import com.chaosbuffalo.mkcore.core.CastInterruptReason;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import com.chaosbuffalo.targeting_api.Targeting;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class PositionFlurryAbility extends MKAbility {
    protected final IntAttribute tickRate = new IntAttribute("tickRate", GameConstants.TICKS_PER_SECOND / 2);
    protected final net.minecraftforge.registries.RegistryObject<? extends PositionTargetingAbility> abilityToCast;

    public PositionFlurryAbility(RegistryObject<? extends PositionTargetingAbility> abilityToCast) {
        super();
        addAttributes(tickRate);
        this.abilityToCast = abilityToCast;
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, Function<Attribute, Float> skillSupplier, MKAbilityInfo abilityInfo) {
        return Component.translatable("mkultra.ability.flurry.description",
                abilityToCast.get().getAbilityName(),
                NUMBER_FORMATTER.format(getDistance(entityData.getEntity())),
                NUMBER_FORMATTER.format(convertDurationToSeconds(tickRate.value())));
    }

    @Override
    public void buildDescription(IMKEntityData casterData, MKAbilityInfo abilityInfo, Consumer<Component> consumer) {
        super.buildDescription(casterData, abilityInfo, consumer);
        abilityToCast.ifPresent(x -> {
            consumer.accept(x.getAbilityName().withStyle(ChatFormatting.UNDERLINE).withStyle(ChatFormatting.GRAY));
            consumer.accept(x.getAbilityDescription(casterData, abilityInfo).plainCopy().withStyle(ChatFormatting.GRAY));
        });
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.PBAOE;
    }

    @Override
    public AbilityType getType() {
        return AbilityType.Ultimate;
    }

    @Override
    public boolean canApplyCastingSpeedModifier() {
        return false;
    }

    @Override
    public boolean isInterruptedBy(IMKEntityData targetData, CastInterruptReason reason) {
        return false;
    }

    @Override
    public void continueCast(LivingEntity castingEntity, IMKEntityData casterData, int castTimeLeft, AbilityContext context,
                             MKAbilityInfo abilityInfo) {
        super.continueCast(castingEntity, casterData, castTimeLeft, context, abilityInfo);
        if (castTimeLeft % tickRate.value() == 0) {
            float dist = getDistance(castingEntity);
            Vec3 minBound = castingEntity.position().subtract(dist, 1.0, dist);
            Vec3 maxBound = castingEntity.position().add(dist, 4.0, dist);
            List<LivingEntity> entities = castingEntity.getCommandSenderWorld().getEntitiesOfClass(LivingEntity.class,
                    new AABB(minBound, maxBound));
            abilityToCast.ifPresent(ab -> {
                for (LivingEntity ent : entities) {
                    if (Targeting.isValidTarget(getTargetContext(), castingEntity, ent)) {
                        ab.castAtPosition(casterData, ent.position(), attr -> abilityInfo.getSkillValue(casterData, attr));
                    }
                }
            });
        }
    }
}
