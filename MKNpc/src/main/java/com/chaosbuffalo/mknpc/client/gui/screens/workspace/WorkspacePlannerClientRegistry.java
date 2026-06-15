package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKTowerWorkspacePlanner;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKWalledKeepWorkspacePlanner;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class WorkspacePlannerClientRegistry {
    public interface PlannerUiDefinition {
        ResourceLocation getPlannerId();

        Component getDisplayName();

        WorkspacePlannerUiContributor createPlannerUi();
    }

    private static final Map<ResourceLocation, PlannerUiDefinition> DEFINITIONS = new LinkedHashMap<>();
    private static final WorkspacePlannerUiContributor FALLBACK = new DefaultPlannerUiContributor();

    private WorkspacePlannerClientRegistry() {
    }

    public static void init() {
        registerInternal(MKTowerWorkspacePlanner.PLANNER_ID,
                Component.literal("Tower"), TowerPlannerUiContributor::new);
        registerInternal(MKWalledKeepWorkspacePlanner.PLANNER_ID,
                Component.literal("Walled Keep"), WalledKeepPlannerUiContributor::new);
    }

    public static void register(PlannerUiDefinition definition) {
        ResourceLocation plannerId = definition.getPlannerId();
        if (DEFINITIONS.containsKey(plannerId)) {
            MKNpc.LOGGER.warn("Ignoring duplicate workspace planner UI registration for {}", plannerId);
            return;
        }
        DEFINITIONS.put(plannerId, definition);
    }

    public static WorkspacePlannerUiContributor getPlannerUi(ResourceLocation plannerId) {
        PlannerUiDefinition definition = DEFINITIONS.get(plannerId);
        return definition == null ? FALLBACK : definition.createPlannerUi();
    }

    public static WorkspacePlannerDraftAdapter getDraftAdapter(ResourceLocation plannerId) {
        return getPlannerUi(plannerId).createDraftAdapter();
    }

    private static void registerInternal(ResourceLocation plannerId, Component displayName,
                                         Supplier<WorkspacePlannerUiContributor> factory) {
        register(new PlannerUiDefinition() {
            @Override
            public ResourceLocation getPlannerId() {
                return plannerId;
            }

            @Override
            public Component getDisplayName() {
                return displayName;
            }

            @Override
            public WorkspacePlannerUiContributor createPlannerUi() {
                return factory.get();
            }
        });
    }
}
