package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
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
    void floorMaskFallbacksPreferLowerConnectivityForLowSprawl() {
        assertEquals(List.of("none", "n", "ew", "new"),
                MKJigsawPlacement.chooseFloorMasks(0.0f,
                        List.of("none", "n", "ew", "new"), RandomSource.create(1L)));
    }

    @Test
    void floorMaskFallbacksPreferHigherConnectivityForHighSprawl() {
        assertEquals(List.of("new", "ew", "n", "none"),
                MKJigsawPlacement.chooseFloorMasks(1.0f,
                        List.of("none", "n", "ew", "new"), RandomSource.create(1L)));
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

    @Test
    void topologyGroupRuleCodecCarriesLinkSettings() {
        MKDungeonTopologyGroupRule rule = new MKDungeonTopologyGroupRule(
                "tower.primary.main_floor",
                1,
                1,
                0,
                0.35f,
                true,
                0.75f,
                12,
                2,
                48,
                Optional.of(42L),
                true,
                null
        );

        JsonElement encoded = MKDungeonTopologyGroupRule.CODEC.encodeStart(JsonOps.INSTANCE, rule)
                .getOrThrow();
        MKDungeonTopologyGroupRule decoded = MKDungeonTopologyGroupRule.CODEC.parse(JsonOps.INSTANCE, encoded)
                .getOrThrow();

        assertTrue(decoded.linksEnabled());
        assertEquals(0.75f, decoded.linkDensity());
        assertEquals(12, decoded.maxLinksPerFloor());
        assertEquals(2, decoded.maxLinksPerRoom());
        assertEquals(48, decoded.maxLinkLength());
    }

    @Test
    void pieceMetadataCodecCarriesFullFloorPalette() {
        ResourceLocation floorBlock = ResourceLocation.parse("minecraft:polished_deepslate");
        ResourceLocation wallBlock = ResourceLocation.parse("minecraft:deepslate_bricks");
        ResourceLocation ceilingBlock = ResourceLocation.parse("minecraft:dark_oak_planks");
        MKJigsawPieceMetadata metadata = new MKJigsawPieceMetadata(
                MKJigsawPieceRole.ROOM,
                0,
                0,
                true,
                false,
                false,
                false,
                "tower.primary.main_floor",
                false,
                false,
                "",
                "",
                0,
                0,
                0,
                0,
                true,
                true,
                false,
                "",
                MKWorkspaceFoundationPolicy.none(),
                floorBlock,
                wallBlock,
                ceilingBlock,
                List.of(),
                List.of(),
                List.of()
        );

        JsonElement encoded = MKJigsawPieceMetadata.CODEC.encodeStart(JsonOps.INSTANCE, metadata)
                .getOrThrow();
        MKJigsawPieceMetadata decoded = MKJigsawPieceMetadata.CODEC.parse(JsonOps.INSTANCE, encoded)
                .getOrThrow();

        assertEquals(floorBlock, decoded.floorBlock());
        assertEquals(wallBlock, decoded.wallBlock());
        assertEquals(ceilingBlock, decoded.ceilingBlock());
    }
}
