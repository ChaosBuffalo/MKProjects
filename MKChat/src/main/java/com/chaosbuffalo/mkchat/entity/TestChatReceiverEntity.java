package com.chaosbuffalo.mkchat.entity;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.capabilities.INpcDialogue;
import com.chaosbuffalo.mkchat.init.ChatEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;

public class TestChatReceiverEntity extends Pig {

    public TestChatReceiverEntity(final EntityType<? extends TestChatReceiverEntity> entityType, Level world) {
        super(entityType, world);
        setCustomName(new TextComponent("Talking Pig"));
        INpcDialogue.get(this).ifPresent(cap -> cap.setDialogueTree(new ResourceLocation(MKChat.MODID, "test")));
    }

    public TestChatReceiverEntity(Level world) {
        this(ChatEntityTypes.TEST_CHAT.get(), world);
    }


    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(1, new RandomLookAroundGoal(this));
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
        if (!player.level.isClientSide()) {
            INpcDialogue.get(this).ifPresent(cap -> cap.hail((ServerPlayer) player));

        }
        return InteractionResult.SUCCESS;
    }
}
