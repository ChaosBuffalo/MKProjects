package com.chaosbuffalo.mkworkspace.init;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.capability.MKStructureWorkspaceDataHandler;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class MKWorkspaceAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES, MKWorkspace.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MKStructureWorkspaceDataHandler>>
            STRUCTURE_WORKSPACE_DATA = ATTACHMENT_TYPES.register(
            "structure_workspace_data", () -> AttachmentType.serializable(holder -> {
                if (holder instanceof Level level) {
                    return new MKStructureWorkspaceDataHandler(level);
                }
                throw new IllegalArgumentException("Cannot attach structure workspace data to non level holder " + holder);
            }).build()
    );

    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }
}
