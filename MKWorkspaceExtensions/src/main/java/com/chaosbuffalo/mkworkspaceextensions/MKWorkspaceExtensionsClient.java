package com.chaosbuffalo.mkworkspaceextensions;

import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspacePlannerClientRegistry;
import com.chaosbuffalo.mkworkspaceextensions.client.gui.screens.workspace.HubSpokePlannerClientContributor;
import com.chaosbuffalo.mkworkspaceextensions.world.gen.workspace.planner.HubSpokePlanner;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = MKWorkspaceExtensions.MODID, dist = Dist.CLIENT)
public class MKWorkspaceExtensionsClient {
    public MKWorkspaceExtensionsClient(IEventBus modBus, ModContainer modContainer) {
        modBus.addListener(this::clientSetup);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> WorkspacePlannerClientRegistry.register(new WorkspacePlannerClientRegistry.PlannerClientDefinition() {
            @Override
            public net.minecraft.resources.ResourceLocation getPlannerId() {
                return HubSpokePlanner.PLANNER_ID;
            }

            @Override
            public Component getDisplayName() {
                return Component.literal("Hub Spoke");
            }

            @Override
            public com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspacePlannerClientContributor createClientContributor() {
                return new HubSpokePlannerClientContributor();
            }
        }));
    }
}
