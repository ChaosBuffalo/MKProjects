package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class WorkspacePlannerUiRegistry {
    public interface PlannerUiDefinition {
        ResourceLocation getPlannerId();

        Component getDisplayName();

        WorkspaceTopologyUiContributor createTopologyUi();
    }

    private static final Map<ResourceLocation, PlannerUiDefinition> DEFINITIONS = new LinkedHashMap<>();
    private static final WorkspaceTopologyUiContributor FALLBACK = new DefaultTopologyUiContributor();

    private WorkspacePlannerUiRegistry() {
    }

    public static void init() {
        registerInternal(MKWorkspaceTopologyProfile.TOWER_PLANNER_ID,
                Component.literal("Tower"), TowerTopologyUiContributor::new);
        registerInternal(MKWorkspaceTopologyProfile.WALLED_KEEP_PLANNER_ID,
                Component.literal("Walled Keep"), WalledKeepTopologyUiContributor::new);
    }

    public static void register(PlannerUiDefinition definition) {
        ResourceLocation plannerId = definition.getPlannerId();
        if (DEFINITIONS.containsKey(plannerId)) {
            MKNpc.LOGGER.warn("Ignoring duplicate workspace planner UI registration for {}", plannerId);
            return;
        }
        DEFINITIONS.put(plannerId, definition);
    }

    public static WorkspaceTopologyUiContributor getTopologyUi(ResourceLocation plannerId) {
        PlannerUiDefinition definition = DEFINITIONS.get(plannerId);
        return definition == null ? FALLBACK : definition.createTopologyUi();
    }

    private static void registerInternal(ResourceLocation plannerId, Component displayName,
                                         Supplier<WorkspaceTopologyUiContributor> factory) {
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
            public WorkspaceTopologyUiContributor createTopologyUi() {
                return factory.get();
            }
        });
    }
}
