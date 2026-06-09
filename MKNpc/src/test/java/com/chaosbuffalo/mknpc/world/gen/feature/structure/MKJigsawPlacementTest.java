package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKJigsawPlacementTest {
    @Test
    void zeroSprawlChoosesClosedMaskWhenAvailable() {
        assertEquals("none", MKJigsawPlacement.chooseFloorMask(0.0f,
                List.of("none", "n", "ew", "new"), RandomSource.create(1L)).orElseThrow());
    }

    @Test
    void fullSprawlChoosesLargestAvailableMask() {
        assertEquals("new", MKJigsawPlacement.chooseFloorMask(1.0f,
                List.of("none", "n", "ew", "new"), RandomSource.create(1L)).orElseThrow());
    }

    @Test
    void emptyMaskSetDoesNotOverridePool() {
        assertTrue(MKJigsawPlacement.chooseFloorMask(1.0f, List.of(), RandomSource.create(1L)).isEmpty());
    }

    @Test
    void topologyGroupRuleCodecCarriesLockedLayoutSeed() {
        MKDungeonTopologyGroupRule rule = new MKDungeonTopologyGroupRule(
                "tower.primary.main_floor",
                1,
                1,
                0,
                0.35f,
                Optional.of(42L),
                true,
                null
        );

        JsonElement encoded = MKDungeonTopologyGroupRule.CODEC.encodeStart(JsonOps.INSTANCE, rule)
                .getOrThrow();
        MKDungeonTopologyGroupRule decoded = MKDungeonTopologyGroupRule.CODEC.parse(JsonOps.INSTANCE, encoded)
                .getOrThrow();

        assertEquals(Optional.of(42L), decoded.lockedLayoutSeed());
        assertEquals(0.35f, decoded.sprawl());
    }
}
