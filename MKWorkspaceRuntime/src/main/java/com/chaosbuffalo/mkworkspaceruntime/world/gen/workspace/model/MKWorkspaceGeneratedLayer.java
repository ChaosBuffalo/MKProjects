package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Arrays;

public enum MKWorkspaceGeneratedLayer {
    WORKSPACE_IDENTITY("workspace_identity"),
    PLANNER_TOPOLOGY("planner_topology"),
    ROOM_ENVELOPES("room_envelopes"),
    CONNECTOR_GRAPH("connector_graph"),
    HALLWAY_ROUTING("hallway_routing"),
    HALLWAY_PIECES("hallway_pieces"),
    TEMPLATE_BINDINGS("template_bindings"),
    SCAFFOLD_BLOCKS("scaffold_blocks"),
    SIDECAR_BLOCKS("sidecar_blocks"),
    PREVIEW_LAYOUT("preview_layout"),
    RUNTIME_METADATA("runtime_metadata");

    public static final Codec<MKWorkspaceGeneratedLayer> CODEC = Codec.STRING.comapFlatMap(
            value -> fromSerializedName(value)
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "unknown workspace generated layer: " + value)),
            MKWorkspaceGeneratedLayer::getSerializedName
    );

    private final String serializedName;

    MKWorkspaceGeneratedLayer(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public static java.util.Optional<MKWorkspaceGeneratedLayer> fromSerializedName(String serializedName) {
        return Arrays.stream(values())
                .filter(layer -> layer.serializedName.equals(serializedName))
                .findFirst();
    }
}
