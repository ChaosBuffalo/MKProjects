package com.chaosbuffalo.mkworkspace.world.gen.workspace.change;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class MKWorkspaceChangeRegistryTest {
    @Test
    void rejectsDuplicateOperationIds() {
        MKWorkspaceChangeRegistry registry = new MKWorkspaceChangeRegistry();
        registry.register(new StringOperation());

        assertThrows(IllegalStateException.class, () -> registry.register(new StringOperation()));
    }

    private static final class StringOperation implements MKWorkspaceChangeOperation<String> {
        @Override
        public ResourceLocation id() {
            return MKWorkspace.id("test_duplicate");
        }

        @Override
        public Codec<String> codec() {
            return Codec.STRING;
        }

        @Override
        public MKWorkspacePreparedChange prepare(ServerPlayer player, BlockPos anchor,
                                                 MKWorkspaceChangeRequest request, String payload) {
            throw new UnsupportedOperationException();
        }
    }
}
