package com.chaosbuffalo.mknpc.world.gen.workspace.mutation;

import com.chaosbuffalo.mknpc.block_entities.MKWorkspaceDevBlockEntity;
import com.chaosbuffalo.mknpc.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mknpc.world.gen.workspace.export.MKWorkspaceBackupManifestWriter;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.StructureBlockEntity;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;

public class MKWorkspaceIdentityRenameService {
    private static final ResourceLocation EMPTY_POOL = ResourceLocation.parse("minecraft:empty");

    private final MKWorkspaceBackupManifestWriter backupWriter = new MKWorkspaceBackupManifestWriter();

    public record RenameResult(MKStructureWorkspace workspace, Path backupPath, int pieceCount) {
    }

    public RenameResult rename(ServerLevel level, MKStructureWorkspace workspace, String namespace,
                               String structureName) throws IOException {
        MKWorkspaceBackupManifestWriter.WrittenBackup backup =
                backupWriter.writeBeforeMutation(level, workspace, "identity-rename");
        MKStructureWorkspace renamed = new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                namespace,
                structureName,
                workspace.familyType(),
                workspace.topologyProfile(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                workspace.familyDefinitions(),
                workspace.openingProfiles(),
                workspace.linearRunFamilies(),
                workspace.createdAt(),
                System.currentTimeMillis(),
                renamePieces(level, workspace, namespace, structureName)
        );
        IMKStructureWorkspaceData.get(level).updateWorkspace(renamed);
        syncBlockEntity(level, renamed);
        return new RenameResult(renamed, backup.path(), renamed.pieces().size());
    }

    private List<MKWorkspacePieceDefinition> renamePieces(ServerLevel level, MKStructureWorkspace workspace,
                                                          String namespace, String structureName) {
        return workspace.pieces().stream()
                .map(piece -> renamePiece(level, workspace, piece, namespace, structureName))
                .toList();
    }

    private MKWorkspacePieceDefinition renamePiece(ServerLevel level, MKStructureWorkspace workspace,
                                                   MKWorkspacePieceDefinition piece, String namespace,
                                                   String structureName) {
        updateStructureBlock(level, piece, namespace, structureName);
        updateSign(level, piece, namespace, structureName);
        List<MKWorkspaceConnectorDefinition> connectors = piece.connectors().stream()
                .map(connector -> renameConnector(level, workspace, piece, connector, namespace, structureName))
                .toList();
        return new MKWorkspacePieceDefinition(
                piece.pieceId(),
                piece.workspaceId(),
                piece.pieceName(),
                piece.roleId(),
                piece.variantIndex(),
                piece.effectiveDimensions(),
                piece.shellMargin(),
                connectors,
                piece.worldOrigin(),
                piece.exportBounds(),
                piece.previewBounds(),
                piece.structureBlockPos(),
                piece.signPos(),
                piece.markerPositions(),
                piece.generatedStairPositions(),
                new LinkedHashMap<>(piece.tags())
        );
    }

    private MKWorkspaceConnectorDefinition renameConnector(ServerLevel level, MKStructureWorkspace workspace,
                                                           MKWorkspacePieceDefinition piece,
                                                           MKWorkspaceConnectorDefinition connector,
                                                           String namespace, String structureName) {
        ResourceLocation targetPool = remapPool(workspace, connector.targetPool(), namespace, structureName);
        ResourceLocation incomingPool = remapPool(workspace, connector.incomingPool(), namespace, structureName);
        ResourceLocation jigsawName = remapNamespace(workspace, connector.jigsawName(), namespace);
        ResourceLocation jigsawTarget = remapNamespace(workspace, connector.jigsawTarget(), namespace);
        BlockPos connectorPos = piece.worldOrigin().offset(connector.relativePos());
        BlockEntity entity = level.getBlockEntity(connectorPos);
        if (entity instanceof JigsawBlockEntity jigsaw) {
            jigsaw.setName(jigsawName);
            jigsaw.setTarget(jigsawTarget);
            jigsaw.setPool(ResourceKey.create(Registries.TEMPLATE_POOL, targetPool));
            jigsaw.setChanged();
        }
        return new MKWorkspaceConnectorDefinition(
                connector.role(),
                connector.facing(),
                connector.relativePos(),
                connector.openingWidth(),
                connector.openingHeight(),
                connector.lateralOffset(),
                connector.verticalOffset(),
                jigsawName,
                jigsawTarget,
                targetPool,
                incomingPool
        );
    }

    private ResourceLocation remapPool(MKStructureWorkspace workspace, ResourceLocation pool, String namespace,
                                       String structureName) {
        if (pool.equals(EMPTY_POOL)) {
            return pool;
        }
        String oldPrefix = workspace.structureName() + "/";
        if (pool.getNamespace().equals(workspace.namespace()) && pool.getPath().startsWith(oldPrefix)) {
            return ResourceLocation.fromNamespaceAndPath(namespace,
                    structureName + "/" + pool.getPath().substring(oldPrefix.length()));
        }
        return pool;
    }

    private ResourceLocation remapNamespace(MKStructureWorkspace workspace, ResourceLocation resource,
                                            String namespace) {
        if (!resource.getNamespace().equals(workspace.namespace())) {
            return resource;
        }
        return ResourceLocation.fromNamespaceAndPath(namespace, resource.getPath());
    }

    private void updateStructureBlock(ServerLevel level, MKWorkspacePieceDefinition piece, String namespace,
                                      String structureName) {
        BlockEntity entity = level.getBlockEntity(piece.structureBlockPos());
        if (entity instanceof StructureBlockEntity structureBlock) {
            structureBlock.setStructureName(ResourceLocation.fromNamespaceAndPath(namespace,
                    structureName + "/" + piece.pieceName()));
            structureBlock.setChanged();
        }
    }

    private void updateSign(ServerLevel level, MKWorkspacePieceDefinition piece, String namespace, String structureName) {
        BlockEntity entity = level.getBlockEntity(piece.signPos());
        if (entity instanceof SignBlockEntity sign) {
            SignText text = sign.getFrontText()
                    .setMessage(0, Component.literal(namespace))
                    .setMessage(1, Component.literal(structureName))
                    .setMessage(2, Component.literal(piece.roleId()))
                    .setMessage(3, Component.literal(piece.pieceName()));
            sign.setText(text, true);
            sign.setText(text, false);
            sign.setChanged();
        }
    }

    private void syncBlockEntity(ServerLevel level, MKStructureWorkspace workspace) {
        BlockEntity blockEntity = level.getBlockEntity(workspace.anchor());
        if (blockEntity instanceof MKWorkspaceDevBlockEntity workspaceDevBlockEntity) {
            workspaceDevBlockEntity.setWorkspaceId(workspace.id());
        }
    }
}
