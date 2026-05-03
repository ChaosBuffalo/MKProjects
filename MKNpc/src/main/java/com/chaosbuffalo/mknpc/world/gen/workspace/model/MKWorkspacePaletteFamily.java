package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import java.util.Optional;

public interface MKWorkspacePaletteFamily {
    String paletteFamilyId();

    Optional<MKTowerWorkspaceCategory> paletteCategory();

    Optional<MKWorkspacePaletteOverride> paletteOverride();
}
