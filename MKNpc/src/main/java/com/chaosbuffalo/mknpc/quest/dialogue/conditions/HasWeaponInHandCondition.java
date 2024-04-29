package com.chaosbuffalo.mknpc.quest.dialogue.conditions;

import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueConditionType;
import com.chaosbuffalo.mknpc.dialogue.NpcDialogueConditionTypes;
import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;

public class HasWeaponInHandCondition extends DialogueCondition {
    private static final HasWeaponInHandCondition INSTANCE = new HasWeaponInHandCondition();
    public static final Codec<HasWeaponInHandCondition> CODEC = Codec.unit(INSTANCE);

    @Override
    public DialogueConditionType<? extends DialogueCondition> getType() {
        return NpcDialogueConditionTypes.HAS_WEAPON_IN_HAND.get();
    }

    @Override
    public boolean meetsCondition(ServerPlayer player, LivingEntity source) {
        Item mainHand = player.getMainHandItem().getItem();
        return mainHand instanceof SwordItem || mainHand instanceof AxeItem || mainHand instanceof ProjectileWeaponItem || mainHand instanceof TridentItem;
    }

    @Override
    public HasWeaponInHandCondition copy() {
        return this;
    }
}
