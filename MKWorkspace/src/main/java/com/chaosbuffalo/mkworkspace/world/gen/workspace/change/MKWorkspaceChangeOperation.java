package com.chaosbuffalo.mkworkspace.world.gen.workspace.change;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** The single extension point for adding persistent workspace mutations. */
public interface MKWorkspaceChangeOperation<T> {
    ResourceLocation id();

    Codec<T> codec();

    MKWorkspacePreparedChange prepare(ServerPlayer player, BlockPos anchor, MKWorkspaceChangeRequest request,
                                      T payload) throws Exception;
}
