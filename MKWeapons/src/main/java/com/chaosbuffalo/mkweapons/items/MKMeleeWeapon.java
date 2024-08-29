package com.chaosbuffalo.mkweapons.items;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.item.IReceivesSkillChange;
import com.chaosbuffalo.mkcore.utils.EntityUtils;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.capabilities.IWeaponData;
import com.chaosbuffalo.mkweapons.capabilities.MKCurioItemHandler;
import com.chaosbuffalo.mkweapons.capabilities.WeaponsCapabilities;
import com.chaosbuffalo.mkweapons.items.accessories.MKAccessory;
import com.chaosbuffalo.mkweapons.items.effects.accesory.IAccessoryEffect;
import com.chaosbuffalo.mkweapons.items.effects.melee.IMeleeWeaponEffect;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import com.chaosbuffalo.mkweapons.items.weapon.tier.IMKTier;
import com.chaosbuffalo.mkweapons.items.weapon.types.IMeleeWeaponType;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MKMeleeWeapon extends SwordItem implements IMKMeleeWeapon, IReceivesSkillChange {
    private final IMeleeWeaponType weaponType;
    private final IMKTier mkTier;
    private final List<IMeleeWeaponEffect> weaponEffects;
    protected Multimap<Holder<Attribute>, AttributeModifier> modifiers;
    protected static final ResourceLocation ATTACK_REACH_MODIFIER = MKWeapons.id("innate_attack_reach");
    protected static final ResourceLocation CRIT_CHANCE_MODIFIER = MKWeapons.id("innate_crit_chance");
    protected static final ResourceLocation CRIT_MULT_MODIFIER = MKWeapons.id("innate_crit_multiplier");
    protected static final ResourceLocation MAX_POISE_MODIFIER = MKWeapons.id("innate_max_poise");
    protected static final ResourceLocation BLOCK_EFFICIENCY_MODIFIER = MKWeapons.id("innate_block_efficiency");
    public static final Set<ItemAbility> SWORD_ACTIONS = ImmutableSet.of(ItemAbilities.SWORD_DIG, ItemAbilities.SHIELD_BLOCK);

    public MKMeleeWeapon(IMKTier tier, IMeleeWeaponType weaponType, Properties builder) {
        super(tier, Math.round(weaponType.getDamageForTier(tier) - tier.getAttackDamageBonus()), weaponType.getAttackSpeed(), builder);
        this.weaponType = weaponType;
        this.mkTier = tier;
        this.weaponEffects = new ArrayList<>();
        recalculateModifiers();
        weaponEffects.addAll(tier.getTierEffects());
        weaponEffects.addAll(weaponType.getWeaponEffects());
    }

    protected void recalculateModifiers() {
        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID,
                getDamage(), AttributeModifier.Operation.ADD_VALUE));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID,
                getWeaponType().getAttackSpeed(), AttributeModifier.Operation.ADD_VALUE));
        builder.put(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(ATTACK_REACH_MODIFIER,
                getWeaponType().getReach(), AttributeModifier.Operation.ADD_VALUE));
        builder.put(MKAttributes.MELEE_CRIT, new AttributeModifier(CRIT_CHANCE_MODIFIER,
                getWeaponType().getCritChance(), AttributeModifier.Operation.ADD_VALUE));
        builder.put(MKAttributes.MELEE_CRIT_MULTIPLIER, new AttributeModifier(CRIT_MULT_MODIFIER,
                getWeaponType().getCritMultiplier(), AttributeModifier.Operation.ADD_VALUE));
        builder.put(MKAttributes.MAX_POISE, new AttributeModifier(MAX_POISE_MODIFIER,
                getWeaponType().getMaxPoise(), AttributeModifier.Operation.ADD_VALUE));
        builder.put(MKAttributes.BLOCK_EFFICIENCY, new AttributeModifier(BLOCK_EFFICIENCY_MODIFIER,
                getWeaponType().getBlockEfficiency(), AttributeModifier.Operation.ADD_VALUE));
        modifiers = builder.build();
    }

    public int getDamage() {
        return Math.round(getWeaponType().getDamageForTier(getMKTier()) - getMKTier().getAttackDamageBonus());
    }


    @Override
    public int getDamage(ItemStack stack) {
        return Math.round(getWeaponType().getDamageForTier(getMKTier()) - getMKTier().getAttackDamageBonus());
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        return super.getDefaultAttributeModifiers(stack);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
        return equipmentSlot == EquipmentSlot.MAINHAND ? this.modifiers : super.getDefaultAttributeModifiers(equipmentSlot);
    }

    @Override
    public void reload() {
        weaponEffects.clear();
        recalculateModifiers();
        weaponEffects.addAll(getMKTier().getTierEffects());
        weaponEffects.addAll(getWeaponType().getWeaponEffects());
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        return false;
    }

    @Override
    public List<IMeleeWeaponEffect> getWeaponEffects() {
        return weaponEffects;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.isBlocking()) {
            MKCore.getEntityData(attacker).ifPresent(attackerData -> {
                if (attackerData.getCombatExtension().getAttackStrengthTicks() >= EntityUtils.getCooldownPeriod(attacker)) {
                    for (IMeleeWeaponEffect effect : getWeaponEffects(stack)) {
                        effect.onHit(this, stack, attackerData, target);
                    }
                    List<MKCurioItemHandler> curios = MKAccessory.getMKCurios(attacker);
                    for (MKCurioItemHandler handler : curios) {
                        for (IAccessoryEffect effect : handler.getEffects()) {
                            effect.onMeleeHit(this, stack, attackerData, target);
                        }
                    }
                }

            });
        }

        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return stack.getCapability(WeaponsCapabilities.WEAPON_DATA_CAPABILITY).map(x -> x.getAttributeModifiers(slot))
                .orElse(getDefaultAttributeModifiers(slot));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack itemstack = playerIn.getItemInHand(handIn);
        ItemStack offhand = playerIn.getOffhandItem();
        if (offhand.getItem() instanceof ShieldItem) {
            return InteractionResultHolder.pass(itemstack);
        }
        if (MKCore.getPlayer(playerIn).map(x -> x.getStats().isPoiseBroke()).orElse(false)) {
            return InteractionResultHolder.pass(itemstack);
        } else {
            playerIn.startUsingItem(handIn);
            return InteractionResultHolder.consume(itemstack);
        }

    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return SWORD_ACTIONS.contains(itemAbility);
    }

    @Nullable
    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        // This needs to be handled carefully.
        // If an SEntityEquipmentPacket is created in LivingEntity#handleHandSwap and sent to all tracking entities
        // the eventual ItemStack serialization may be performed by multiple network threads, causing a
        // ConcurrentModificationException in CompoundNBT if they insert data into the ItemStack's tag simultaneously.
        // Work around this by returning a new CompoundNBT for each call, embedding the original share tag and putting
        // our data next to it without modifying the actual ItemStack tag

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

    @Override
    public IMeleeWeaponType getWeaponType() {
        return weaponType;
    }

    @Override
    public IMKTier getMKTier() {
        return mkTier;
    }

    public void addToTooltip(ItemStack stack, @Nullable Player player, List<Component> tooltip) {
        if (getWeaponType().isTwoHanded()) {
            tooltip.add(Component.translatable("mkweapons.two_handed.name")
                    .withStyle(ChatFormatting.GRAY));
            if (Screen.hasShiftDown()) {
                tooltip.add(Component.translatable("mkweapons.two_handed.description"));
            }
        }
        for (IMeleeWeaponEffect effect : getWeaponEffects(stack)) {
            effect.addInformation(stack, player, tooltip);
        }
        MKAbility ability = getAbility(stack);
        if (ability != null) {
            tooltip.add(Component.translatable("mkweapons.grants_ability",
                    ability.getAbilityName()).withStyle(ChatFormatting.GOLD));
        }
    }


    @Override
    public List<IMeleeWeaponEffect> getWeaponEffects(ItemStack item) {
        return item.getCapability(WeaponsCapabilities.WEAPON_DATA_CAPABILITY).map(cap -> {
            if (cap.hasMeleeWeaponEffects()) {
                return cap.getMeleeEffects();
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
    public void onSkillChange(ItemStack stack, Player playerEntity) {
        getWeaponEffects(stack).forEach(x -> x.onSkillChange(playerEntity));
    }
}
