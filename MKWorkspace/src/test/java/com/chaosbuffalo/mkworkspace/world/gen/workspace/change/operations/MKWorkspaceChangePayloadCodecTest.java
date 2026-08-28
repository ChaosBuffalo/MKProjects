package com.chaosbuffalo.mkworkspace.world.gen.workspace.change.operations;

import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangePayloads;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceStairMode;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceStairRiseType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtOps;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MKWorkspaceChangePayloadCodecTest {
    @Test
    void insertSocketPayloadRoundTrips() {
        MKWorkspaceInsertSocketChangePayload expected = new MKWorkspaceInsertSocketChangePayload(
                UUID.randomUUID(), new BlockPos(4, 70, -8), Direction.NORTH, true, "alcove",
                5, 4, 3, 1, 2, "minecraft:stone", "minecraft:air");

        var encoded = MKWorkspaceChangePayloads.encode(MKWorkspaceInsertSocketChangePayload.CODEC, expected,
                "insert test");
        MKWorkspaceInsertSocketChangePayload decoded = MKWorkspaceInsertSocketChangePayload.CODEC
                .parse(NbtOps.INSTANCE, encoded).getOrThrow();

        assertEquals(expected, decoded);
    }

    @Test
    void stairPayloadRoundTrips() {
        MKWorkspaceSimpleChangePayload expected = new MKWorkspaceSimpleChangePayload("tower/stair",
                MKWorkspaceStairMode.AUTO.getSerializedName(),
                MKWorkspaceStairRiseType.STAIR.getSerializedName(), 3);

        var encoded = MKWorkspaceChangePayloads.encode(MKWorkspaceSimpleChangePayload.CODEC, expected,
                "stairs test");
        MKWorkspaceSimpleChangePayload decoded = MKWorkspaceSimpleChangePayload.CODEC
                .parse(NbtOps.INSTANCE, encoded).getOrThrow();

        assertEquals(expected, decoded);
    }
}
