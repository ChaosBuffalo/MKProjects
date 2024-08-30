package com.chaosbuffalo.mkweapons.items;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.item.IReceivesSkillChange;
import com.chaosbuffalo.mkcore.utils.EntityUtils;
import com.chaosbuffalo.mkweapons.components.MeleeEffectsComponent;
import com.chaosbuffalo.mkweapons.components.WeaponsComponents;
import com.chaosbuffalo.mkweapons.items.accessories.MKAccessories;
import com.chaosbuffalo.mkweapons.items.effects.accesory.IAccessoryEffect;
import com.chaosbuffalo.mkweapons.items.effects.melee.IMeleeWeaponEffect;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import com.chaosbuffalo.mkweapons.items.weapon.tier.IMKTier;
import com.chaosbuffalo.mkweapons.items.weapon.types.IMeleeWeaponType;
import com.google.common.collect.ImmutableSet;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.util.ConcatenatedListView;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class MKMeleeWeapon extends SwordItem implements IMKMeleeWeapon, IReceivesSkillChange {
    private final IMeleeWeaponType weaponType;
    private final IMKTier mkTier;
    private final List<IMeleeWeaponEffect> weaponEffects;
    public static final Set<ItemAbility> SWORD_ACTIONS = ImmutableSet.of(ItemAbilities.SWORD_DIG, ItemAbilities.SHIELD_BLOCK);

    public MKMeleeWeapon(IMKTier tier, IMeleeWeaponType weaponType, Properties builder) {
        super(tier, builder);
        this.weaponType = weaponType;
        this.mkTier = tier;
        this.weaponEffects = ConcatenatedListView.of(
                tier.getTierEffects(),
                weaponType.getWeaponEffects()
        );
    }

    public static ItemAttributeModifiers createAttributes(IMKTier tier, IMeleeWeaponType weaponType) {
        ResourceLocation modId = weaponType.getName().withSuffix("_" + tier.getName());
        return ItemAttributeModifiers.builder()
                .add(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(BASE_ATTACK_DAMAGE_ID, calculateDamage(tier, weaponType), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(
                        Attributes.ATTACK_SPEED,
                        new AttributeModifier(BASE_ATTACK_SPEED_ID, weaponType.getAttackSpeed(), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(
                        Attributes.ENTITY_INTERACTION_RANGE,
                        new AttributeModifier(modId, weaponType.getReach(), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(
                        MKAttributes.MELEE_CRIT,
                        new AttributeModifier(modId, weaponType.getCritChance(), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(
                        MKAttributes.MELEE_CRIT_MULTIPLIER,
                        new AttributeModifier(modId, weaponType.getCritMultiplier(), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(
                        MKAttributes.MAX_POISE,
                        new AttributeModifier(modId, weaponType.getMaxPoise(), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(
                        MKAttributes.BLOCK_EFFICIENCY,
                        new AttributeModifier(modId, weaponType.getBlockEfficiency(), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND
                )
                .build();
    }


    static int calculateDamage(IMKTier mkTier, IMeleeWeaponType weaponType) {
        return Math.round(weaponType.getDamageForTier(mkTier) - mkTier.getAttackDamageBonus());
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        return false;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.isBlocking()) {
            MKCore.getEntityData(attacker).ifPresent(attackerData -> {
                if (attackerData.getCombatExtension().getAttackStrengthTicks() >= EntityUtils.getCooldownPeriod(attacker)) {
                    for (IMeleeWeaponEffect effect : getWeaponEffects(stack)) {
                        effect.onHit(this, stack, attackerData, target);
                    }

                    MKAccessories.iterateAccessories(attacker, (accStack, accessory) -> {
                        for (IAccessoryEffect effect : accessory.getAccessoryEffects(accStack)) {
                            effect.onMeleeHit(this, stack, attackerData, target);
                        }
                    });
                }

            });
        }

        return super.hurtEnemy(stack, target, attacker);
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
        MeleeEffectsComponent stackEffects = item.get(WeaponsComponents.MELEE_EFFECTS);
        if (stackEffects != null) {
            return ConcatenatedListView.of(weaponEffects, stackEffects.effects());
        } else {
            return weaponEffects;
        }
    }

    @Nullable
    @Override
    public MKAbility getAbility(ItemStack itemStack) {
        var ability = itemStack.get(WeaponsComponents.WEAPON_ABILITY);
        if (ability != null) {
            return ability.abilityHolder().value();
        } else {
            return null;
        }
    }

    @Override
    public void onSkillChange(ItemStack stack, Player playerEntity) {
        getWeaponEffects(stack).forEach(x -> x.onSkillChange(playerEntity));
    }
}
