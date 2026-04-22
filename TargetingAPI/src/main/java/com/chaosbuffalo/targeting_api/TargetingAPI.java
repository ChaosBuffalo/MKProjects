package com.chaosbuffalo.targeting_api;


import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;


/**
 * Provides an extensible targeting utility API for defining how entities and players can interact with
 * each other. All relationships are abstracted into one of: Friendly, Enemy, Neutral, or Unhandled.
 * <p>
 * The library centers on relationship resolution between entities and reusable
 * {@link TargetingContext targeting contexts} that describe valid targets such
 * as self, friendly, enemy, neutral, players, or custom filtered groups.
 * <p>
 *
 */
@Mod(TargetingAPI.MODID)
public class TargetingAPI {

    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MODID = "targeting_api";

    /**
     * Creates the mod entry point.
     *
     * @param modEventBus the module event bus provided by NeoForge
     * @param modContainer the owning mod container
     */
    public TargetingAPI(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, TargetingAPI::onServerTickStart);
        NeoForge.EVENT_BUS.addListener(TargetingAPI::onPlayerChangeGameMode);
    }

    private static void onServerTickStart(ServerTickEvent.Pre event) {
        Targeting.clearTickCaches();
    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onClientTickStart(ClientTickEvent.Pre event) {
            Targeting.clearTickCaches();
        }
    }

    private static void onPlayerChangeGameMode(PlayerEvent.PlayerChangeGameModeEvent event) {
        GameType from = event.getCurrentGameMode();
        GameType to = event.getNewGameMode();
        // Creative/spectator status affects relation resolution — invalidate if crossing that boundary
        if (affectsTargeting(from) != affectsTargeting(to)) {
            Targeting.invalidateAllRelations();
        }
    }

    private static boolean affectsTargeting(GameType mode) {
        return mode == GameType.CREATIVE || mode == GameType.SPECTATOR;
    }
}
