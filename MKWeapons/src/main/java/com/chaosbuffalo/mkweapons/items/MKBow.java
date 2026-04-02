package com.chaosbuffalo.mkweapons.items;

import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.item.IReceivesSkillChange;
import com.chaosbuffalo.mkweapons.components.RangedEffectsComponent;
import com.chaosbuffalo.mkweapons.components.WeaponsComponents;
import com.chaosbuffalo.mkweapons.items.effects.ranged.IRangedWeaponEffect;
import com.chaosbuffalo.mkweapons.items.effects.ranged.RangedSkillScalingEffect;
import com.chaosbuffalo.mkweapons.items.weapon.IMKRangedWeapon;
import com.chaosbuffalo.mkweapons.items.weapon.tier.IMKTier;
import com.chaosbuffalo.mkweapons.items.weapon.types.IMeleeWeaponType;
import com.chaosbuffalo.mkweapons.items.weapon.types.IRangedWeaponType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.ConcatenatedListView;
import net.neoforged.neoforge.event.EventHooks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MKBow extends BowItem implements IMKRangedWeapon, IReceivesSkillChange {
    private final List<IRangedWeaponEffect> weaponEffects = new ArrayList<>();
    private final IMKTier tier;
    private final IRangedWeaponType rangedType;

    public MKBow(Properties builder, IMKTier tier, IRangedWeaponType rangedWeaponType) {
        super(builder);
        this.tier = tier;
        this.rangedType = rangedWeaponType;
        this.weaponEffects.addAll(tier.getRangedEffects());
        this.weaponEffects.add(new RangedSkillScalingEffect(
                rangedWeaponType.getBaseDamage() + tier.getAttackDamageBonus(), MKAttributes.MARKSMANSHIP));
    }

    public IRangedWeaponType getRangedWeaponType() {
        return rangedType;
    }

    public float getDrawTime(ItemStack item, LivingEntity entity) {
        float time = rangedType.getBaseDrawTime();
        for (IRangedWeaponEffect weaponEffect : getWeaponEffects(item)) {
            time = weaponEffect.modifyDrawTime(time, item, entity);
        }
        return time;
    }

    public float getPowerFactor(int useTicks, ItemStack stack, LivingEntity entity) {
        float powerFactor = (float) (useTicks) / getDrawTime(stack, entity);
        powerFactor = (powerFactor * powerFactor + powerFactor * 2.0F) / 3.0F;
        if (powerFactor > 1.0F) {
            powerFactor = 1.0F;
        }
        return powerFactor;
    }

    public float getLaunchVelocity(ItemStack stack, LivingEntity entity) {
        float vel = rangedType.getBaseLaunchVelocity();
        for (IRangedWeaponEffect weaponEffect : getWeaponEffects(stack)) {
            vel = weaponEffect.modifyLaunchVelocity(vel, stack, entity);
        }
        return vel;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof Player player) {
            ItemStack itemstack = player.getProjectile(stack);
            if (!itemstack.isEmpty()) {
                int i = this.getUseDuration(stack, entityLiving) - timeLeft;
                i = EventHooks.onArrowLoose(stack, level, player, i, !itemstack.isEmpty());
                if (i < 0) {
                    return;
                }

                float f = getPowerFactor(i, stack, entityLiving);
                if (!((double) f < 0.1)) {
                    List<ItemStack> list = draw(stack, itemstack, player);
                    if (level instanceof ServerLevel serverLevel) {
                        if (!list.isEmpty()) {
                            float velocity = getLaunchVelocity(stack, entityLiving);
                            this.shoot(serverLevel, player, player.getUsedItemHand(), stack, list, f * velocity, 1.0F, f == 1.0F, null);
                        }
                    }

                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + f * 0.5F);
                    player.awardStat(Stats.ITEM_USED.get(this));
                }
            }
        }

    }

    @Override
    public AbstractArrow customArrow(AbstractArrow arrow, ItemStack projectileStack, ItemStack weaponStack) {
        // set item stack on cap here
        Entity shooter = arrow.getOwner();
        double damage = arrow.getBaseDamage();
        damage += getMKTier().getAttackDamageBonus();
        if (shooter instanceof LivingEntity shootingEntity) {
            for (IRangedWeaponEffect weaponEffect : getWeaponEffects(weaponStack)) {
                damage = weaponEffect.modifyArrowDamage(damage, shootingEntity, arrow);
            }
        }
        arrow.setBaseDamage(damage);
        return super.customArrow(arrow, projectileStack, weaponStack);
    }

    public void addToTooltip(ItemStack stack, @Nullable Player player, List<Component> tooltip) {
        if (getMKTier().getAttackDamageBonus() > 0) {
            tooltip.add(Component.translatable("mkweapons.bow_extra_damage.description",
                    getMKTier().getAttackDamageBonus()).withStyle(ChatFormatting.GRAY));
        }
        for (IRangedWeaponEffect weaponEffect : getWeaponEffects(stack)) {
            weaponEffect.addInformation(stack, player, tooltip);
        }
    }


    @Override
    public IMKTier getMKTier() {
        return tier;
    }

    @Override
    public List<IRangedWeaponEffect> getWeaponEffects(ItemStack item) {
        RangedEffectsComponent stackEffects = item.get(WeaponsComponents.RANGED_EFFECTS);
        if (stackEffects != null) {
            return ConcatenatedListView.of(weaponEffects, stackEffects.effects());
        } else {
            return weaponEffects;
        }
    }

    @Override
    public void onSkillChange(ItemStack itemStack, Player playerEntity, Holder<Attribute> skill) {
        getWeaponEffects(itemStack).forEach(x -> x.onSkillChange(playerEntity, skill));
    }

    public static ItemAttributeModifiers.Builder createAttributes(IMKTier tier, IRangedWeaponType weaponType) {
        ResourceLocation modifierId = weaponType.getName().withSuffix("_" + tier.getName());
        return ItemAttributeModifiers.builder()
                .add(
                        MKAttributes.RANGED_CRIT,
                        new AttributeModifier(
                                modifierId, 0.05, AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                ).add(
                        MKAttributes.RANGED_CRIT_MULTIPLIER,
                        new AttributeModifier(
                                modifierId, 0.25, AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                );
    }
}
