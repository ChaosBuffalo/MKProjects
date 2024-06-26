package com.chaosbuffalo.mkfaction.event;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.utils.RayTraceUtils;
import com.chaosbuffalo.mkfaction.MKFactionMod;
import com.chaosbuffalo.mkfaction.capabilities.FactionCapabilities;
import com.chaosbuffalo.mkfaction.client.gui.FactionPage;
import com.chaosbuffalo.mkfaction.faction.PlayerFactionStatus;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = MKFactionMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class InputHandler {

    public static final KeyMapping CON_KEY_BIND = new KeyMapping("key.mkfaction.con.desc",
            InputConstants.KEY_C,
            "key.mkfaction.category");
    public static final KeyMapping FACTION_PANEL_KEY_BIND = new KeyMapping("key.mkfaction.panel.desc",
            InputConstants.KEY_P,
            "key.mkfaction.category");

    @Mod.EventBusSubscriber(modid = MKFactionMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerKeyBinding(RegisterKeyMappingsEvent event) {
            event.register(CON_KEY_BIND);
            event.register(FACTION_PANEL_KEY_BIND);
        }
    }

    @Nullable
    public static <E extends Entity> EntityHitResult getLookingAtNonPlayer(Class<E> clazz,
                                                                           final Entity mainEntity,
                                                                           double distance) {
        HitResult result = RayTraceUtils.getLookingAt(clazz, mainEntity, 30.f, e -> !(e instanceof Player));
        return result instanceof EntityHitResult ? (EntityHitResult) result : null;
    }

    private static void handleInputEvent() {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        while (CON_KEY_BIND.consumeClick()) {
            EntityHitResult trace = getLookingAtNonPlayer(LivingEntity.class, player, 30.0f);
            if (trace != null && trace.getType() != HitResult.Type.MISS) {
                Entity target = trace.getEntity();
                target.getCapability(FactionCapabilities.MOB_FACTION_CAPABILITY).ifPresent(mobFaction ->
                        player.getCapability(FactionCapabilities.PLAYER_FACTION_CAPABILITY).ifPresent(playerFaction -> {
                            PlayerFactionStatus status = playerFaction.getFactionStatus(mobFaction);
                            MutableComponent msg = Component.translatable(status.getTranslationKey() + ".con",
                                    target.getName()).withStyle(status.getColor());
                            if (player.isCreative()) {
                                msg.append(String.format(" (%s)", mobFaction.getFactionName()));
                            }
                            player.sendSystemMessage(msg);
                        }));
            }
        }
        while (FACTION_PANEL_KEY_BIND.consumeClick()) {
            MKCore.getPlayer(player).ifPresent(playerData -> Minecraft.getInstance().setScreen(new FactionPage(playerData)));
        }
    }

    @SubscribeEvent
    public static void onMouseEvent(InputEvent.MouseButton event) {
        handleInputEvent();
    }

    @SubscribeEvent
    public static void onKeyEvent(InputEvent.Key event) {
        handleInputEvent();
    }
}
