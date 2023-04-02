package com.chaosbuffalo.mkweapons.items.effects.melee;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class UndeadDamageMeleeWeaponEffect extends BaseMeleeWeaponEffect {
    private float damageMultiplier;
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "weapon_effect.undead_damage");

    public UndeadDamageMeleeWeaponEffect(float multiplier){
        this();
        this.damageMultiplier = multiplier;
    }

    public UndeadDamageMeleeWeaponEffect(){
        super(NAME, ChatFormatting.GOLD);
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        damageMultiplier = dynamic.get("multiplier").asFloat(1.5f);
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("multiplier"), ops.createFloat(damageMultiplier));
    }

    @Override
    public float modifyDamageDealt(float damage, IMKMeleeWeapon weapon, ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target.isInvertedHealAndHarm()){
            return damage * damageMultiplier;
        } else {
            return damage;
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip) {
        super.addInformation(stack, worldIn, tooltip);
        if (Screen.hasShiftDown()){
            tooltip.add(new TextComponent(I18n.get("mkweapons.weapon_effect.undead_damage.description",
                    damageMultiplier)));
        }
    }
}
