package com.chaosbuffalo.mkweapons.items;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.item.IReceivesSkillChange;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.capabilities.IWeaponData;
import com.chaosbuffalo.mkweapons.capabilities.WeaponsCapabilities;
import com.chaosbuffalo.mkweapons.items.effects.ranged.IRangedWeaponEffect;
import com.chaosbuffalo.mkweapons.items.effects.ranged.RangedSkillScalingEffect;
import com.chaosbuffalo.mkweapons.items.weapon.IMKRangedWeapon;
import com.chaosbuffalo.mkweapons.items.weapon.tier.MKTier;
import com.google.common.collect.Multimap;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.stats.Stats;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.world.item.Item.Properties;

public class MKBow extends BowItem implements IMKRangedWeapon, IReceivesSkillChange {
    private final List<IRangedWeaponEffect> weaponEffects = new ArrayList<>();
    private final MKTier tier;
    private final float baseDrawTime;
    private final float baseLaunchVel;

    public MKBow(ResourceLocation location, Properties builder, MKTier tier, float baseDrawTime,
                 float baseLaunchVel, IRangedWeaponEffect... weaponEffects) {
        super(builder);
        setRegistryName(location);
        this.baseDrawTime = baseDrawTime;
        this.baseLaunchVel = baseLaunchVel;
        this.weaponEffects.addAll(Arrays.asList(weaponEffects));
        this.weaponEffects.add(new RangedSkillScalingEffect(5.0 + tier.getAttackDamageBonus(), MKAttributes.MARKSMANSHIP));
        this.tier = tier;
    }

    public float getDrawTime(ItemStack item, LivingEntity entity){
        float time = baseDrawTime;
        for (IRangedWeaponEffect weaponEffect : getWeaponEffects(item)){
            time = weaponEffect.modifyDrawTime(time, item, entity);
        }
        return time;
    }

    @Nullable
    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        // See comment in MKMeleeWeapon#getShareTag
        CompoundTag newTag = new CompoundTag();
        CompoundTag original = super.getShareTag(stack);
        if (original != null) {
            newTag.put("share", original);
        }
        stack.getCapability(WeaponsCapabilities.WEAPON_DATA_CAPABILITY).ifPresent(weaponData ->
                newTag.put("weaponCap", weaponData.serializeNBT()));
        return newTag;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag shareTag) {
        if (shareTag == null)
            return;

        if (shareTag.contains("share")) {
            super.readShareTag(stack, shareTag.getCompound("share"));
        }
        if (shareTag.contains("weaponCap")) {
            stack.getCapability(WeaponsCapabilities.WEAPON_DATA_CAPABILITY).ifPresent(weaponData ->
                    weaponData.deserializeNBT(shareTag.getCompound("weaponCap")));
        }
    }

    public float getPowerFactor(int useTicks, ItemStack stack, LivingEntity entity){
        float powerFactor = (float)(useTicks) / getDrawTime(stack, entity);
        powerFactor = (powerFactor * powerFactor + powerFactor * 2.0F) / 3.0F;
        if (powerFactor > 1.0F) {
            powerFactor = 1.0F;
        }
        return powerFactor;
    }

    public float getLaunchVelocity(ItemStack stack, LivingEntity entity){
        float vel = baseLaunchVel;
        for (IRangedWeaponEffect weaponEffect : getWeaponEffects(stack)){
            vel = weaponEffect.modifyLaunchVelocity(vel, stack, entity);
        }
        return vel;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return stack.getCapability(WeaponsCapabilities.WEAPON_DATA_CAPABILITY).map(x -> x.getAttributeModifiers(slot))
                .orElse(getDefaultAttributeModifiers(slot));
    }


    @Override
    public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof Player) {
            Player player = (Player)entityLiving;
            boolean doesntNeedAmmo = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(
                    Enchantments.INFINITY_ARROWS, stack) > 0;
            ItemStack ammoStack = player.getProjectile(stack);

            int useTicks = this.getUseDuration(stack) - timeLeft;
            useTicks = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(stack, worldIn, player, useTicks, !ammoStack.isEmpty() || doesntNeedAmmo);
            if (useTicks < 0) return;

            if (!ammoStack.isEmpty() || doesntNeedAmmo) {
                if (ammoStack.isEmpty()) {
                    ammoStack = new ItemStack(Items.ARROW);
                }

                float powerFactor = getPowerFactor(useTicks, stack, entityLiving);
                if (!((double)powerFactor < 0.1D)) {
                    boolean hasAmmo = player.getAbilities().instabuild || (ammoStack.getItem() instanceof ArrowItem && ((ArrowItem)ammoStack.getItem()).isInfinite(ammoStack, stack, player));
                    if (!worldIn.isClientSide) {
                        ArrowItem arrowItem = (ArrowItem)(ammoStack.getItem() instanceof ArrowItem ? ammoStack.getItem() : Items.ARROW);
                        AbstractArrow arrowEntity = arrowItem.createArrow(worldIn, ammoStack, player);
                        arrowEntity = customArrow(arrowEntity, stack);
                        arrowEntity.shootFromRotation(entityLiving, entityLiving.getXRot(), entityLiving.getYRot(),
                                0.0F, powerFactor * getLaunchVelocity(stack, entityLiving), 1.0F);
                        if (powerFactor == 1.0F) {
                            arrowEntity.setCritArrow(true);
                        }

                        int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
                        if (powerLevel > 0) {
                            arrowEntity.setBaseDamage(arrowEntity.getBaseDamage() + (double)powerLevel * 0.5D + 0.5D);
                        }

                        int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
                        if (punchLevel > 0) {
                            arrowEntity.setKnockback(punchLevel);
                        }

                        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
                            arrowEntity.setSecondsOnFire(100);
                        }

                        stack.hurtAndBreak(1, player, (ent) -> ent.broadcastBreakEvent(ent.getUsedItemHand()));
                        if (hasAmmo || player.getAbilities().instabuild && (ammoStack.getItem() == Items.SPECTRAL_ARROW || ammoStack.getItem() == Items.TIPPED_ARROW)) {
                            arrowEntity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                        }
                        worldIn.addFreshEntity(arrowEntity);
                    }

                    worldIn.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F,
                            1.0F / (entityLiving.getRandom().nextFloat() * 0.4F + 1.2F) + powerFactor * 0.5F);
                    if (!hasAmmo && !player.getAbilities().instabuild) {
                        ammoStack.shrink(1);
                        if (ammoStack.isEmpty()) {
                            player.getInventory().removeItem(ammoStack);
                        }
                    }
                    player.awardStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }


    public AbstractArrow customArrow(AbstractArrow arrow, ItemStack stack) {
        // set item stack on cap here
        Entity shooter = arrow.getOwner();
        double damage = arrow.getBaseDamage();
        damage += getMKTier().getAttackDamageBonus();
        if (shooter instanceof LivingEntity){
            MKWeapons.getArrowCapability(arrow).ifPresent(cap ->
                    cap.setShootingWeapon(((LivingEntity) shooter).getMainHandItem()));
            for (IRangedWeaponEffect weaponEffect : getWeaponEffects(stack)){
                damage = weaponEffect.modifyArrowDamage(damage, (LivingEntity) shooter, arrow);
            }
        }
        arrow.setBaseDamage(damage);
        return super.customArrow(arrow);
    }

    public void addToTooltip(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip){
        if (getMKTier().getAttackDamageBonus() > 0){
            tooltip.add(new TranslatableComponent("mkweapons.bow_extra_damage.description",
                    getMKTier().getAttackDamageBonus()).withStyle(ChatFormatting.GRAY));
        }
        for (IRangedWeaponEffect weaponEffect : getWeaponEffects(stack)){
            weaponEffect.addInformation(stack, worldIn, tooltip);
        }
    }


    @Override
    public MKTier getMKTier() {
        return tier;
    }

    @Override
    public List<IRangedWeaponEffect> getWeaponEffects() {
        return weaponEffects;
    }

    @Override
    public List<IRangedWeaponEffect> getWeaponEffects(ItemStack item) {
        return item.getCapability(WeaponsCapabilities.WEAPON_DATA_CAPABILITY).map(cap -> {
            if (cap.hasRangedWeaponEffects()){
                return cap.getRangedEffects();
            } else {
                return weaponEffects;
            }
        }).orElse(weaponEffects);
    }

    @Nullable
    @Override
    public MKAbility getAbility(ItemStack itemStack) {
        return MKCoreRegistry.getAbility(itemStack.getCapability(WeaponsCapabilities.WEAPON_DATA_CAPABILITY)
                .map(IWeaponData::getAbilityName).orElse(MKCoreRegistry.INVALID_ABILITY));
    }

    @Override
    public void onSkillChange(ItemStack itemStack, Player playerEntity) {
        getWeaponEffects(itemStack).forEach(x -> x.onSkillChange(playerEntity));
    }
}
