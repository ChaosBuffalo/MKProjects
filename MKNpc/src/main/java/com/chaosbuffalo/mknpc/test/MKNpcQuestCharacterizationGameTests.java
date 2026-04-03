package com.chaosbuffalo.mknpc.test;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IPlayerQuestingData;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.capabilities.PlayerQuestingDataHandler;
import com.chaosbuffalo.mknpc.content.ContentDB;
import com.chaosbuffalo.mknpc.quest.Quest;
import com.chaosbuffalo.mknpc.quest.QuestDefinition;
import com.chaosbuffalo.mknpc.quest.QuestRegistries;
import com.chaosbuffalo.mknpc.quest.generation.QuestChainBuildResult;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.CommonListenerCookie;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(MKNpc.MODID)
@PrefixGameTestTemplate(false)
public class MKNpcQuestCharacterizationGameTests {
    @GameTest(template = "test")
    public static void startQuestSeedsFirstLinearQuest(GameTestHelper helper) {
        IWorldNpcData worldData = IWorldNpcData.get(helper.getLevel());
        helper.assertTrue(ContentDB.tryGetPrimaryData().isPresent(), "Primary world quest DB should be available");

        QuestDefinition definition = createLinearDefinition("start_linear", "first", "second");
        QuestChainBuildResult buildResult = worldData.buildQuest(definition, helper.absolutePos(net.minecraft.core.BlockPos.ZERO))
                .orElseThrow(() -> new AssertionError("Failed to build test quest chain"));

        ServerPlayer player = createPlayer(helper, "quest-start");
        IPlayerQuestingData questData = MKNpc.getPlayerQuestData(player)
                .orElseThrow(() -> new AssertionError("Player quest attachment missing"));

        questData.startQuest(worldData, buildResult.instance.getQuestId());

        helper.assertValueEqual(questData.getQuestStatus(buildResult.instance.getQuestId()),
                PlayerQuestingDataHandler.QuestStatus.IN_PROGRESS, "quest status");
        helper.assertValueEqual(questData.getCurrentQuestSteps(buildResult.instance.getQuestId()).size(), 1, "current quest count");
        helper.assertValueEqual(questData.getCurrentQuestSteps(buildResult.instance.getQuestId()).getFirst(), "first", "current quest name");
        helper.succeed();
    }

    @GameTest(template = "test")
    public static void advancingLinearQuestMovesForwardAndCompletes(GameTestHelper helper) {
        IWorldNpcData worldData = IWorldNpcData.get(helper.getLevel());
        QuestDefinition definition = createLinearDefinition("advance_linear", "first", "second");
        QuestChainBuildResult buildResult = worldData.buildQuest(definition, helper.absolutePos(net.minecraft.core.BlockPos.ZERO))
                .orElseThrow(() -> new AssertionError("Failed to build test quest chain"));

        ServerPlayer player = createPlayer(helper, "quest-advance");
        IPlayerQuestingData questData = MKNpc.getPlayerQuestData(player)
                .orElseThrow(() -> new AssertionError("Player quest attachment missing"));

        questData.startQuest(worldData, buildResult.instance.getQuestId());
        questData.advanceQuestChain(worldData, buildResult.instance, definition.getQuest("first"));

        helper.assertValueEqual(questData.getQuestStatus(buildResult.instance.getQuestId()),
                PlayerQuestingDataHandler.QuestStatus.IN_PROGRESS, "status after first advance");
        helper.assertValueEqual(questData.getCurrentQuestSteps(buildResult.instance.getQuestId()).getFirst(), "second", "next quest name");

        questData.advanceQuestChain(worldData, buildResult.instance, definition.getQuest("second"));

        helper.assertValueEqual(questData.getQuestStatus(buildResult.instance.getQuestId()),
                PlayerQuestingDataHandler.QuestStatus.COMPLETED, "status after final advance");
        helper.succeed();
    }

    private static QuestDefinition createLinearDefinition(String id, String... questNames) {
        ResourceKey<QuestDefinition> key = ResourceKey.create(QuestRegistries.QUEST_DEFINITIONS, MKNpc.id(id));
        QuestDefinition definition = new QuestDefinition(key);
        definition.setMode(QuestDefinition.QuestMode.LINEAR);
        definition.setQuestName(Component.literal(id));
        for (String questName : questNames) {
            definition.addQuest(new Quest(questName, Component.literal(questName)));
        }
        return definition;
    }

    private static ServerPlayer createPlayer(GameTestHelper helper, String name) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), name), false);
        ServerPlayer player = new ServerPlayer(
                helper.getLevel().getServer(),
                helper.getLevel(),
                cookie.gameProfile(),
                cookie.clientInformation()
        );
        new ServerGamePacketListenerImpl(helper.getLevel().getServer(), new Connection(PacketFlow.SERVERBOUND), player, cookie);
        MKCore.getPlayerOrThrow(player).getPersonaManager().onJoinLevel();
        return player;
    }
}
