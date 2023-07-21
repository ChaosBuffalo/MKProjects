package com.chaosbuffalo.mknpc.event;

import com.chaosbuffalo.mkcore.init.CoreParticles;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.NpcCapabilities;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestData;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestObjectiveData;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@Mod.EventBusSubscriber(modid = MKNpc.MODID, value = Dist.CLIENT)
public class NpcClientEventHandler {

    private static final KeyMapping questMenuBind = new KeyMapping("key.hud.questmenu",
            InputConstants.KEY_K, "key.mknpc.category");
    private static int ticks = -1;

    @Mod.EventBusSubscriber(modid = MKNpc.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerKeyBinding(RegisterKeyMappingsEvent event) {
            event.register(questMenuBind);
        }
    }

    @SubscribeEvent
    public static void onKeyEvent(InputEvent.Key event) {
        handleInputEvent();
    }

    public static void handleInputEvent() {
//        PlayerEntity player = Minecraft.getInstance().player;
//        if (player == null)
//            return;
//
//        MKPlayerData playerData = MKCore.getPlayerOrNull(player);
//        if (playerData == null)
//            return;
//
//        while (questMenuBind.isPressed()) {
//            Minecraft.getInstance().displayGuiScreen(new QuestPage());
//        }
    }

    @SubscribeEvent
    public static void onRenderLast(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player != null && event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {

            if (player.tickCount != ticks) {
                ticks = player.tickCount;
                Set<GlobalPos> alreadySeen = new HashSet<>();
                player.getCapability(NpcCapabilities.PLAYER_QUEST_DATA_CAPABILITY).ifPresent(x -> {
                    x.getQuestChains().forEach(pQuestChain -> {
                        pQuestChain.getCurrentQuests().forEach(questName -> {
                            PlayerQuestData playerQuestData = pQuestChain.getQuestData(questName);
                            for (PlayerQuestObjectiveData objectiveData : playerQuestData.getObjectives()) {
                                if (!objectiveData.isComplete()) {
                                    Map<String, GlobalPos> posMap = objectiveData.getBlockPosData();
                                    for (GlobalPos pos : posMap.values()) {
                                        if (pos.dimension().equals(player.getCommandSenderWorld().dimension()) && !alreadySeen.contains(pos)) {
                                            event.getLevelRenderer().addParticle(CoreParticles.INDICATOR_PARTICLE.get(), true,
                                                    pos.pos().getX() + 0.5, pos.pos().getY() + 1.0,
                                                    pos.pos().getZ() + 0.5, 0.0, 0.0, 0.0);
                                            alreadySeen.add(pos);
                                        }

                                    }
                                }
                            }
                        });

                    });
                });
            }

        }
    }
}
