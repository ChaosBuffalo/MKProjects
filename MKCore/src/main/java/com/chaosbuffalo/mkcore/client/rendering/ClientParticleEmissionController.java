package com.chaosbuffalo.mkcore.client.rendering;

import com.chaosbuffalo.mkcore.MKConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ClientParticleEmissionController {
    private static final double EXPIRATION_SECONDS = 5.0;
    private static final double CLEANUP_INTERVAL_SECONDS = 1.0;

    private static final Map<EmitterKey, EmitterState> EMITTERS = new HashMap<>();
    private static double lastCleanupTime = Double.NEGATIVE_INFINITY;

    private ClientParticleEmissionController() {
    }

    public static int consumeEmissions(EmitterKey key) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return 1;
        }
        double currentTime = getCurrentTimeSeconds(mc);
        if (currentTime < lastCleanupTime) {
            EMITTERS.clear();
            lastCleanupTime = currentTime;
        }
        if (lastCleanupTime == Double.NEGATIVE_INFINITY || currentTime - lastCleanupTime >= CLEANUP_INTERVAL_SECONDS) {
            cleanupExpired(currentTime);
        }

        double emissionsPerSecond = Math.max(1.0, MKConfig.CLIENT.particleEmitterUpdatesPerSecond.get());
        double period = 1.0 / emissionsPerSecond;
        EmitterState state = EMITTERS.get(key);
        if (state == null) {
            EMITTERS.put(key, new EmitterState(0.0, currentTime));
            return 1;
        }

        double delta = Math.max(0.0, currentTime - state.lastSeenTime);
        state.lastSeenTime = currentTime;
        state.accumulatedTime = Math.min(state.accumulatedTime + delta, period * emissionsPerSecond);

        int emissions = 0;
        while (state.accumulatedTime >= period) {
            state.accumulatedTime -= period;
            emissions++;
        }
        return emissions;
    }

    public static EmitterKey forCastingHand(Entity entity, ResourceLocation animationId, String handName) {
        return new EmitterKey("casting_hand", entity.getUUID(), animationId, handName);
    }

    public static EmitterKey forBoneEffect(Entity entity, UUID instanceId, String boneName) {
        return new EmitterKey("bone_effect", entity.getUUID(), null, instanceId + ":" + boneName);
    }

    public static EmitterKey forHeldItemAttachment(Entity entity, UUID instanceId, String handName, int attachmentIndex) {
        return new EmitterKey("held_item_attachment", entity.getUUID(), null, instanceId + ":" + handName + ":" + attachmentIndex);
    }

    private static void cleanupExpired(double currentTime) {
        EMITTERS.entrySet().removeIf(entry -> currentTime - entry.getValue().lastSeenTime > EXPIRATION_SECONDS);
        lastCleanupTime = currentTime;
    }

    private static double getCurrentTimeSeconds(Minecraft mc) {
        return (mc.level.getGameTime() + mc.getTimer().getGameTimeDeltaPartialTick(true)) / 20.0;
    }

    public record EmitterKey(String category, UUID entityId, ResourceLocation animationId, String detail) {
    }

    private static final class EmitterState {
        private double accumulatedTime;
        private double lastSeenTime;

        private EmitterState(double accumulatedTime, double lastSeenTime) {
            this.accumulatedTime = accumulatedTime;
            this.lastSeenTime = lastSeenTime;
        }
    }
}
