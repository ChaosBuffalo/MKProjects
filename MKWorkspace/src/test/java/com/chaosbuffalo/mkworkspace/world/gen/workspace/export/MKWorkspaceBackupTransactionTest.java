package com.chaosbuffalo.mkworkspace.world.gen.workspace.export;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MKWorkspaceBackupTransactionTest {
    @Test
    void persistentMutationRequiresCoordinatorTransactionScope() {
        assertThrows(IllegalStateException.class,
                () -> MKWorkspaceBackupManifestWriter.requireTransaction("test"));

        try (MKWorkspaceBackupManifestWriter.TransactionScope ignored =
                     MKWorkspaceBackupManifestWriter.enterTransaction(null)) {
            assertDoesNotThrow(() -> MKWorkspaceBackupManifestWriter.requireTransaction("test"));
        }

        assertThrows(IllegalStateException.class,
                () -> MKWorkspaceBackupManifestWriter.requireTransaction("test"));
    }
}
