package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKTowerWorkspacePlanner;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKWalledKeepWorkspacePlanner;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class WorkspacePlannerClientRegistry {
    public interface PlannerClientDefinition {
        ResourceLocation getPlannerId();

        Component getDisplayName();

        WorkspacePlannerClientContributor createClientContributor();
    }

    private static final Map<ResourceLocation, PlannerClientDefinition> DEFINITIONS = new LinkedHashMap<>();
    private static final WorkspacePlannerClientContributor FALLBACK = new DefaultPlannerClientContributor();

    private WorkspacePlannerClientRegistry() {
    }

    public static void init() {
        registerInternal(MKTowerWorkspacePlanner.PLANNER_ID,
                Component.literal("Tower"), TowerPlannerClientContributor::new);
        registerInternal(MKWalledKeepWorkspacePlanner.PLANNER_ID,
                Component.literal("Walled Keep"), WalledKeepPlannerClientContributor::new);
    }

    public static void register(PlannerClientDefinition definition) {
        ResourceLocation plannerId = definition.getPlannerId();
        if (DEFINITIONS.containsKey(plannerId)) {
            MKNpc.LOGGER.warn("Ignoring duplicate workspace planner client registration for {}", plannerId);
            return;
        }
        DEFINITIONS.put(plannerId, definition);
    }

    public static WorkspacePlannerClientContributor getClientContributor(ResourceLocation plannerId) {
        PlannerClientDefinition definition = DEFINITIONS.get(plannerId);
        return definition == null ? FALLBACK : definition.createClientContributor();
    }

    public static WorkspacePlannerDraftAdapter getDraftAdapter(ResourceLocation plannerId) {
        return getClientContributor(plannerId).createDraftAdapter();
    }

    public static List<PlannerClientDefinition> plannerClientDefinitions() {
        return List.copyOf(DEFINITIONS.values());
    }

    private static void registerInternal(ResourceLocation plannerId, Component displayName,
                                         Supplier<WorkspacePlannerClientContributor> factory) {
        register(new PlannerClientDefinition() {
            @Override
            public ResourceLocation getPlannerId() {
                return plannerId;
            }

            @Override
            public Component getDisplayName() {
                return displayName;
            }

            @Override
            public WorkspacePlannerClientContributor createClientContributor() {
                return factory.get();
            }
        });
    }
}
