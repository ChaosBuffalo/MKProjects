package com.chaosbuffalo.mkworkspace.world.gen.workspace;

import javax.annotation.Nullable;
import java.util.UUID;

public interface MKWorkspaceAnchor {
    @Nullable
    UUID getWorkspaceId();

    void setWorkspaceId(@Nullable UUID workspaceId);
}
