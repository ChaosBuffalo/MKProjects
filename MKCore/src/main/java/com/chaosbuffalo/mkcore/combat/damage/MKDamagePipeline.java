package com.chaosbuffalo.mkcore.combat.damage;

import net.minecraft.resources.ResourceLocation;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MKDamagePipeline {
    private static final EnumMap<MKDamageStageOrder, LinkedHashMap<ResourceLocation, MKDamageStage>> STAGES =
            new EnumMap<>(MKDamageStageOrder.class);

    static {
        for (MKDamageStageOrder order : MKDamageStageOrder.values()) {
            STAGES.put(order, new LinkedHashMap<>());
        }
    }

    private MKDamagePipeline() {
    }

    public static synchronized void register(MKDamageStageOrder order, ResourceLocation id, MKDamageStage stage) {
        LinkedHashMap<ResourceLocation, MKDamageStage> bucket = STAGES.get(order);
        if (bucket.containsKey(id)) {
            throw new IllegalStateException("Duplicate damage pipeline stage registration for " + id);
        }
        bucket.put(id, stage);
    }

    public static void run(MKDamageContext context) {
        for (MKDamageStageOrder order : MKDamageStageOrder.values()) {
            for (Map.Entry<ResourceLocation, MKDamageStage> entry : STAGES.get(order).entrySet()) {
                // Keep the legacy LivingDamageEvent.Pre handlers and the new context-based stages in sync
                // until all damage code is fully migrated onto MKDamageContext.
                context.addAudit("stage:start:" + entry.getKey());
                context.syncToEvent();
                entry.getValue().apply(context);
                context.syncFromEvent();
                context.addAudit("stage:end:" + entry.getKey() + "=" + context.getWorkingDamage());
            }
        }
    }
}
