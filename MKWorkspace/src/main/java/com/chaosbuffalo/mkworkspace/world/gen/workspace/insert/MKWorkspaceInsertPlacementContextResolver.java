package com.chaosbuffalo.mkworkspace.world.gen.workspace.insert;

import com.chaosbuffalo.mkworkspace.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.Comparator;
import java.util.Optional;

public class MKWorkspaceInsertPlacementContextResolver {
    private final MKWorkspaceInsertFootprintScanner footprintScanner = new MKWorkspaceInsertFootprintScanner();

    public Optional<MKWorkspaceInsertPlacementContext> resolve(ServerLevel level, BlockPos socketWorldPos,
                                                              Direction socketFacing) {
        return IMKStructureWorkspaceData.get(level).getAllWorkspaces().stream()
                .flatMap(workspace -> workspace.pieces().stream()
                        .filter(piece -> piece.exportBounds().isInside(socketWorldPos))
                        .map(piece -> new WorkspacePieceCandidate(workspace, piece)))
                .min(Comparator
                        .comparingInt((WorkspacePieceCandidate candidate) -> volume(candidate.piece().exportBounds()))
                        .thenComparingInt(candidate -> candidate.workspace().anchor().distManhattan(socketWorldPos)))
                .map(candidate -> context(level, candidate.workspace(), candidate.piece(), socketWorldPos,
                        socketFacing));
    }

    public String serializeBlockState(BlockState state) {
        StringBuilder builder = new StringBuilder();
        builder.append(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
        if (!state.getProperties().isEmpty()) {
            builder.append('[');
            boolean first = true;
            for (Property<?> property : state.getProperties()) {
                if (!first) {
                    builder.append(',');
                }
                first = false;
                builder.append(property.getName()).append('=').append(getPropertyValueName(state, property));
            }
            builder.append(']');
        }
        return builder.toString();
    }

    private MKWorkspaceInsertPlacementContext context(ServerLevel level, MKStructureWorkspace workspace,
                                                      MKWorkspacePieceDefinition piece, BlockPos socketWorldPos,
                                                      Direction socketFacing) {
        BlockPos socketLocalPos = socketWorldPos.subtract(piece.worldOrigin());
        return new MKWorkspaceInsertPlacementContext(
                workspace,
                piece,
                socketWorldPos,
                socketLocalPos,
                socketFacing,
                serializeBlockState(level.getBlockState(socketWorldPos)),
                MKWorkspaceInsertTemplateCompatibility.compatibleFamilyIds(level, workspace, socketFacing),
                footprintScanner.scan(level, workspace, piece, socketWorldPos)
        );
    }

    private int volume(BoundingBox bounds) {
        return bounds.getXSpan() * bounds.getYSpan() * bounds.getZSpan();
    }

    private <T extends Comparable<T>> String getPropertyValueName(BlockState state, Property<T> property) {
        return property.getName(state.getValue(property));
    }

    private record WorkspacePieceCandidate(MKStructureWorkspace workspace, MKWorkspacePieceDefinition piece) {
    }
}
