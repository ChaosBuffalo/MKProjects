package com.chaosbuffalo.mkweapons.items.effects.melee;

import com.chaosbuffalo.mkcore.core.CombatExtensionModule;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ResetAttackSwingPacket;
import com.chaosbuffalo.mkcore.utils.EntityUtils;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class DoubleStrikeMeleeWeaponEffect extends BaseMeleeWeaponEffect {

    private double chance;
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "weapon_effect.double_strike");

    public DoubleStrikeMeleeWeaponEffect(double chance) {
        this();
        this.chance = chance;
    }

    public DoubleStrikeMeleeWeaponEffect() {
        super(NAME, ChatFormatting.DARK_AQUA);
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        setChance(dynamic.get("chance").asDouble(0.05));
    }

    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }


    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("chance"), ops.createDouble(getChance()));
    }


    @Override
    public void addInformation(ItemStack stack, @Nullable Player player, List<Component> tooltip) {
        super.addInformation(stack, player, tooltip);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("mkweapons.weapon_effect.double_strike.description",
                    chance * 100.0f));
        }
    }

    @Override
    public void postAttack(IMKMeleeWeapon weapon, ItemStack stack, IMKEntityData attackerData) {
        if (attackerData.isClientSide())
            return;

        LivingEntity attacker = attackerData.getEntity();
        double roll = attacker.getRandom().nextDouble();
        if (roll >= (1.0 - chance)) {
            CombatExtensionModule combatModule = attackerData.getCombatExtension();
            double cooldownPeriod = EntityUtils.getCooldownPeriod(attacker);
            combatModule.increaseAttackStrengthTicks((int) cooldownPeriod);
            if (attacker instanceof ServerPlayer serverPlayer) {
                PacketHandler.sendMessage(new ResetAttackSwingPacket(combatModule.getAttackStrengthTicks()),
                        serverPlayer);
            }
        }
    }
}
