package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKInsertFamilyPools;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKWorkspacePieceGeometryTest {
    @Test
    void courtyardSocketInsertFamiliesAreEmptyExactBoundsScaffold() {
        Map<String, String> tags = Map.of(
                MKWorkspacePieceGeometry.TAG_TOWER_PIECE_KIND, "insert",
                MKInsertFamilyPools.TAG_INSERT_FAMILY_ID, "fire_shrine_platform_contents",
                MKInsertFamilyPools.TAG_INSERT_FAMILY_KIND, MKWorkspaceInsertFamilyKind.INSERT_SOCKET.getSerializedName()
        );

        assertTrue(MKWorkspacePieceGeometry.isExactBoundsScaffold(tags));
        assertTrue(MKWorkspacePieceGeometry.isEmptyScaffold(tags));
        assertFalse(MKWorkspacePieceGeometry.isFloorLinkInsert(tags));
    }

    @Test
    void legacyCourtyardSocketInsertKindStillScaffoldsAsInsertSocket() {
        Map<String, String> tags = Map.of(
                MKWorkspacePieceGeometry.TAG_TOWER_PIECE_KIND, "insert",
                MKInsertFamilyPools.TAG_INSERT_FAMILY_ID, "fire_shrine_platform_contents",
                MKInsertFamilyPools.TAG_INSERT_FAMILY_KIND,
                MKWorkspaceInsertFamilyKind.COURTYARD_SOCKET.getSerializedName()
        );

        assertTrue(MKWorkspacePieceGeometry.isInsertSocketInsert(tags));
        assertTrue(MKWorkspacePieceGeometry.isExactBoundsScaffold(tags));
        assertTrue(MKWorkspacePieceGeometry.isEmptyScaffold(tags));
    }

    @Test
    void floorLinkInsertsStayExactBoundsButKeepTheirScaffold() {
        Map<String, String> tags = Map.of(
                MKWorkspacePieceGeometry.TAG_TOWER_PIECE_KIND,
                MKWorkspacePieceGeometry.TOWER_PIECE_KIND_FLOOR_LINK_INSERT
        );

        assertTrue(MKWorkspacePieceGeometry.isExactBoundsScaffold(tags));
        assertFalse(MKWorkspacePieceGeometry.isEmptyScaffold(tags));
        assertTrue(MKWorkspacePieceGeometry.isFloorLinkInsert(tags));
    }
}
