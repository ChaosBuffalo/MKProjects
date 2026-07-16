package com.chaosbuffalo.mkworkspaceextensions;

import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWorkspacePlannerRegistry;
import com.chaosbuffalo.mkworkspaceextensions.world.gen.workspace.planner.HubSpokePlanner;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(MKWorkspaceExtensions.MODID)
public class MKWorkspaceExtensions {
    public static final String MODID = "mkworkspace_extensions";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MKWorkspaceExtensions(IEventBus modBus, ModContainer modContainer) {
        MKWorkspacePlannerRegistry.registerShared(new HubSpokePlanner());
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
