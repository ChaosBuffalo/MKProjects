package com.chaosbuffalo.mknpc.quest.dialogue.effects;

import com.chaosbuffalo.mkchat.dialogue.DialogueNode;
import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffect;
import com.chaosbuffalo.mknpc.ContentDB;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IPlayerQuestingData;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.capabilities.NpcCapabilities;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class StartQuestChainEffect extends DialogueEffect implements IReceivesChainId {
    public static final ResourceLocation effectTypeName = new ResourceLocation(MKNpc.MODID, "start_quest_chain");
    private UUID chainId;

    public StartQuestChainEffect(UUID chainId) {
        this();
        this.chainId = chainId;
    }

    public StartQuestChainEffect() {

        super(effectTypeName);
        chainId = Util.NIL_UUID;
    }

    @Override
    public StartQuestChainEffect copy() {
        // No runtime mutable state
        return new StartQuestChainEffect(chainId);
    }

    @Override
    public void applyEffect(ServerPlayer serverPlayerEntity, LivingEntity livingEntity, DialogueNode dialogueNode) {
        IPlayerQuestingData questingData = MKNpc.getPlayerQuestData(serverPlayerEntity).resolve().orElse(null);
        if (questingData == null) {
            return;
        }
        IWorldNpcData worldData = ContentDB.getPrimaryData();
        questingData.startQuest(worldData, chainId);
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        chainId = dynamic.get("chainId").asString().result().map(UUID::fromString).orElse(Util.NIL_UUID);
    }


    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        if (!chainId.equals(Util.NIL_UUID)) {
            builder.put(ops.createString("chainId"), ops.createString(chainId.toString()));
        }

    }

    @Override
    public void setChainId(UUID chainId) {
        this.chainId = chainId;
    }
}
