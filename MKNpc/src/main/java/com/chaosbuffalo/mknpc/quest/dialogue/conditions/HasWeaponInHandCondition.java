package com.chaosbuffalo.mknpc.quest.dialogue.conditions;

import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mknpc.MKNpc;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;

public class HasWeaponInHandCondition extends DialogueCondition {

    public static final ResourceLocation conditionTypeName = new ResourceLocation(MKNpc.MODID, "has_weapon_in_hand");

    public HasWeaponInHandCondition(){
        super(conditionTypeName);
    }

    @Override
    public boolean meetsCondition(ServerPlayer player, LivingEntity source) {
        Item mainHand = player.getMainHandItem().getItem();
        return mainHand instanceof SwordItem || mainHand instanceof AxeItem || mainHand instanceof ProjectileWeaponItem || mainHand instanceof TridentItem;
    }

    @Override
    public HasWeaponInHandCondition copy() {
        return new HasWeaponInHandCondition();
    }
}

