package com.chaosbuffalo.mkworkspace.world.gen.workspace.insert;

import com.chaosbuffalo.mkworkspace.world.gen.workspace.MKStructureWorkspaceService;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.export.MKWorkspaceBackupManifestWriter;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKInsertSlotPools;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertAttachmentFace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyKind;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertSocketPlacement;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MKWorkspaceInsertAuthoringService {
    private static final ResourceLocation EMPTY_POOL = ResourceLocation.parse("minecraft:empty");
    private final MKWorkspaceInsertFootprintScanner footprintScanner = new MKWorkspaceInsertFootprintScanner();

    public record CreateSocketFamilyRequest(
            BlockPos anchor,
            UUID hostPieceId,
            BlockPos socketWorldPos,
            Direction socketFacing,
            String familyId,
            int width,
            int height,
            int depth,
            int faceUOffset,
            int faceVOffset,
            String hostFinalState,
            String templateJigsawFinalState
    ) {
    }

    public record PlaceExistingSocketRequest(
            BlockPos anchor,
            UUID hostPieceId,
            BlockPos socketWorldPos,
            Direction socketFacing,
            String familyId,
            String hostFinalState
    ) {
    }

    public record Result(Optional<MKWorkspaceInsertPlacementContext> context, List<String> errors) {
        public static Result success(MKWorkspaceInsertPlacementContext context) {
            return new Result(Optional.of(context), List.of());
        }

        public static Result failure(List<String> errors) {
            return new Result(Optional.empty(), List.copyOf(errors));
        }
    }

    public List<String> validateCreateSocketFamily(ServerLevel level, CreateSocketFamilyRequest request) {
        Optional<MKStructureWorkspace> workspaceOpt = IMKStructureWorkspaceData.get(level)
                .getWorkspaceByAnchor(request.anchor());
        if (workspaceOpt.isEmpty()) {
            return List.of("no workspace exists at anchor " + request.anchor().toShortString());
        }
        MKStructureWorkspace workspace = workspaceOpt.get();
        Optional<MKWorkspacePieceDefinition> hostPieceOpt = workspace.pieces().stream()
                .filter(piece -> piece.pieceId().equals(request.hostPieceId())).findFirst();
        if (hostPieceOpt.isEmpty()) {
            return List.of("host template piece no longer exists");
        }
        MKWorkspacePieceDefinition hostPiece = hostPieceOpt.get();
        ArrayList<String> errors = new ArrayList<>();
        if (!hostPiece.exportBounds().isInside(request.socketWorldPos())) {
            errors.add("socket position is outside the host authorial template");
        }
        if (workspace.insertSlots().stream().anyMatch(slot -> slot.slotId().equals(request.familyId()))) {
            errors.add("insert slot " + request.familyId() + " already exists");
        }
        MKWorkspaceInsertAttachmentFace attachmentFace =
                MKWorkspaceInsertAttachmentFace.fromDirection(request.socketFacing().getOpposite());
        MKWorkspaceInsertFamilyDefinition insertFamily = new MKWorkspaceInsertFamilyDefinition(
                request.familyId(), MKWorkspaceInsertFamilyKind.INSERT_SOCKET,
                request.width(), request.height(), request.depth(), Optional.of(attachmentFace),
                request.faceUOffset(), request.faceVOffset(), request.templateJigsawFinalState());
        errors.addAll(insertFamily.validate());
        BlockPos socketLocalPos = request.socketWorldPos().subtract(hostPiece.worldOrigin());
        BoundingBox hostLocalBounds = new BoundingBox(0, 0, 0,
                hostPiece.exportBounds().getXSpan() - 1,
                hostPiece.exportBounds().getYSpan() - 1,
                hostPiece.exportBounds().getZSpan() - 1);
        errors.addAll(MKWorkspaceInsertSocketPlacement.validateFits(hostLocalBounds, socketLocalPos,
                insertFamily.width(), insertFamily.height(), insertFamily.depth(), attachmentFace,
                request.faceUOffset(), request.faceVOffset()));
        if (errors.isEmpty()) {
            BoundingBox candidateBounds = MKWorkspaceInsertSocketPlacement.projectedInsertBounds(hostLocalBounds,
                    socketLocalPos, insertFamily.width(), insertFamily.height(), insertFamily.depth(),
                    attachmentFace, request.faceUOffset(), request.faceVOffset());
            errors.addAll(validateNoCollision(level, workspace, hostPiece, request.socketWorldPos(), candidateBounds));
        }
        if (errors.isEmpty()) {
            errors.addAll(new MKStructureWorkspaceService().validateWorkspace(withInsertFamily(workspace, insertFamily)));
        }
        return List.copyOf(errors);
    }

    public List<String> validatePlaceExistingSocketFamily(ServerLevel level, PlaceExistingSocketRequest request) {
        Optional<MKStructureWorkspace> workspaceOpt = IMKStructureWorkspaceData.get(level)
                .getWorkspaceByAnchor(request.anchor());
        if (workspaceOpt.isEmpty()) {
            return List.of("no workspace exists at anchor " + request.anchor().toShortString());
        }
        MKStructureWorkspace workspace = workspaceOpt.get();
        Optional<MKWorkspacePieceDefinition> hostPieceOpt = workspace.pieces().stream()
                .filter(piece -> piece.pieceId().equals(request.hostPieceId())).findFirst();
        if (hostPieceOpt.isEmpty()) {
            return List.of("host template piece no longer exists");
        }
        MKWorkspacePieceDefinition hostPiece = hostPieceOpt.get();
        ArrayList<String> errors = new ArrayList<>();
        if (!hostPiece.exportBounds().isInside(request.socketWorldPos())) {
            errors.add("socket position is outside the host authorial template");
        }
        Optional<MKWorkspaceInsertFamilyDefinition> familyOpt = workspace.insertSlots().stream()
                .filter(family -> family.slotId().equals(request.familyId()))
                .filter(family -> family.kind() == MKWorkspaceInsertFamilyKind.INSERT_SOCKET).findFirst();
        if (familyOpt.isEmpty()) {
            errors.add("insert slot " + request.familyId() + " does not exist");
            return List.copyOf(errors);
        }
        MKWorkspaceInsertFamilyDefinition family = familyOpt.get();
        MKWorkspaceInsertAttachmentFace templateFace =
                MKWorkspaceInsertAttachmentFace.fromDirection(request.socketFacing().getOpposite());
        if (!MKWorkspaceInsertTemplateCompatibility.hasAttachableTemplateJigsaw(level, workspace, family,
                request.socketFacing())) {
            errors.add("insert slot " + request.familyId() +
                    " has no template variant that can attach to a " +
                    request.socketFacing().getSerializedName() + "-facing socket");
        }
        MKWorkspaceInsertAttachmentFace authoredFace = family.attachmentFace().orElse(templateFace);
        int faceUOffset = family.attachmentFace().isPresent() ? family.faceUOffset() :
                centeredUOffset(family.width(), family.depth(), templateFace);
        int faceVOffset = family.attachmentFace().isPresent() ? family.faceVOffset() :
                centeredVOffset(family.height(), family.depth(), templateFace);
        BoundingBox hostLocalBounds = new BoundingBox(0, 0, 0,
                hostPiece.exportBounds().getXSpan() - 1,
                hostPiece.exportBounds().getYSpan() - 1,
                hostPiece.exportBounds().getZSpan() - 1);
        errors.addAll(MKWorkspaceInsertSocketPlacement.validateOrientedFits(hostLocalBounds,
                request.socketWorldPos().subtract(hostPiece.worldOrigin()), family.width(), family.height(),
                family.depth(), authoredFace, faceUOffset, faceVOffset, templateFace,
                MKWorkspaceInsertTemplateCompatibility.jigsawOrientation(request.socketFacing()).top()));
        if (errors.isEmpty()) {
            BoundingBox candidateBounds = MKWorkspaceInsertSocketPlacement.projectedOrientedInsertBounds(
                    request.socketWorldPos().subtract(hostPiece.worldOrigin()), family.width(), family.height(),
                    family.depth(), authoredFace, faceUOffset, faceVOffset, templateFace,
                    MKWorkspaceInsertTemplateCompatibility.jigsawOrientation(request.socketFacing()).top());
            errors.addAll(validateNoCollision(level, workspace, hostPiece, request.socketWorldPos(), candidateBounds));
        }
        return List.copyOf(errors);
    }

    public Result createSocketFamily(ServerLevel level, CreateSocketFamilyRequest request) {
        MKWorkspaceBackupManifestWriter.requireTransaction("create-insert-socket-family");
        List<String> preparedErrors = validateCreateSocketFamily(level, request);
        if (!preparedErrors.isEmpty()) {
            return Result.failure(preparedErrors);
        }
        Optional<MKStructureWorkspace> workspaceOpt = IMKStructureWorkspaceData.get(level)
                .getWorkspaceByAnchor(request.anchor());
        if (workspaceOpt.isEmpty()) {
            return Result.failure(List.of("no workspace exists at anchor " + request.anchor().toShortString()));
        }
        MKStructureWorkspace workspace = workspaceOpt.get();
        Optional<MKWorkspacePieceDefinition> hostPieceOpt = workspace.pieces().stream()
                .filter(piece -> piece.pieceId().equals(request.hostPieceId()))
                .findFirst();
        if (hostPieceOpt.isEmpty()) {
            return Result.failure(List.of("host template piece no longer exists"));
        }
        MKWorkspacePieceDefinition hostPiece = hostPieceOpt.get();
        if (!hostPiece.exportBounds().isInside(request.socketWorldPos())) {
            return Result.failure(List.of("socket position is outside the host authorial template"));
        }
        if (workspace.insertSlots().stream().anyMatch(slot -> slot.slotId().equals(request.familyId()))) {
            return Result.failure(List.of("insert slot " + request.familyId() + " already exists"));
        }

        MKWorkspaceInsertAttachmentFace attachmentFace =
                MKWorkspaceInsertAttachmentFace.fromDirection(request.socketFacing().getOpposite());
        int faceUOffset = request.faceUOffset();
        int faceVOffset = request.faceVOffset();
        BlockPos socketLocalPos = request.socketWorldPos().subtract(hostPiece.worldOrigin());
        BoundingBox hostLocalBounds = new BoundingBox(0, 0, 0,
                hostPiece.exportBounds().getXSpan() - 1,
                hostPiece.exportBounds().getYSpan() - 1,
                hostPiece.exportBounds().getZSpan() - 1);

        ArrayList<String> errors = new ArrayList<>();
        MKWorkspaceInsertFamilyDefinition insertFamily = new MKWorkspaceInsertFamilyDefinition(
                request.familyId(),
                MKWorkspaceInsertFamilyKind.INSERT_SOCKET,
                request.width(),
                request.height(),
                request.depth(),
                Optional.of(attachmentFace),
                faceUOffset,
                faceVOffset,
                request.templateJigsawFinalState()
        );
        errors.addAll(insertFamily.validate());
        errors.addAll(MKWorkspaceInsertSocketPlacement.validateFits(hostLocalBounds, socketLocalPos,
                insertFamily.width(), insertFamily.height(), insertFamily.depth(), attachmentFace,
                faceUOffset, faceVOffset));
        if (errors.isEmpty()) {
            BoundingBox candidateBounds = MKWorkspaceInsertSocketPlacement.projectedInsertBounds(hostLocalBounds,
                    socketLocalPos, insertFamily.width(), insertFamily.height(), insertFamily.depth(),
                    attachmentFace, faceUOffset, faceVOffset);
            errors.addAll(validateNoCollision(level, workspace, hostPiece, request.socketWorldPos(),
                    candidateBounds));
        }
        if (!errors.isEmpty()) {
            return Result.failure(errors);
        }

        MKStructureWorkspace requestedWorkspace = withInsertFamily(workspace, insertFamily);
        MKStructureWorkspaceService workspaceService = new MKStructureWorkspaceService();
        List<String> workspaceErrors = workspaceService.validateWorkspace(requestedWorkspace);
        if (!workspaceErrors.isEmpty()) {
            return Result.failure(workspaceErrors);
        }
        Optional<MKStructureWorkspace> updatedOpt = workspaceService.createOrUpdateWorkspace(level, requestedWorkspace);
        if (updatedOpt.isEmpty()) {
            return Result.failure(List.of("workspace update failed"));
        }
        MKStructureWorkspace updated = updatedOpt.get();
        MKWorkspacePieceDefinition updatedHostPiece = updated.pieces().stream()
                .filter(piece -> piece.pieceId().equals(request.hostPieceId()))
                .findFirst()
                .orElse(hostPiece);
        Optional<MKWorkspacePieceDefinition> insertTemplatePiece = updated.pieces().stream()
                .filter(piece -> insertFamily.slotId().equals(MKInsertSlotPools.slotId(piece.tags())))
                .filter(piece -> insertFamily.kind().getSerializedName().equals(piece.tags()
                        .get(MKInsertSlotPools.TAG_INSERT_SLOT_KIND)))
                .findFirst();
        if (insertTemplatePiece.isEmpty()) {
            return Result.failure(List.of("workspace update did not create the insert authorial template"));
        }

        ResourceLocation insertPool = MKInsertSlotPools.poolId(updated.namespace(), updated.structureName(),
                insertFamily.slotId());
        placeHostSocketJigsaw(level, request.socketWorldPos(), request.socketFacing(), insertPool,
                safeFinalState(request.hostFinalState()));
        placeInsertTemplateJigsaw(level, insertTemplatePiece.get(), insertFamily, insertPool);

        MKWorkspaceInsertPlacementContext context = new MKWorkspaceInsertPlacementContext(
                updated,
                updatedHostPiece,
                request.socketWorldPos(),
                request.socketWorldPos().subtract(updatedHostPiece.worldOrigin()),
                request.socketFacing(),
                safeFinalState(request.hostFinalState()),
                MKWorkspaceInsertTemplateCompatibility.compatibleFamilyIds(level, updated, request.socketFacing()),
                footprintScanner.scan(level, updated, updatedHostPiece, request.socketWorldPos())
        );
        return Result.success(context);
    }

    public Result placeExistingSocketFamily(ServerLevel level, PlaceExistingSocketRequest request) {
        MKWorkspaceBackupManifestWriter.requireTransaction("place-insert-socket");
        List<String> preparedErrors = validatePlaceExistingSocketFamily(level, request);
        if (!preparedErrors.isEmpty()) {
            return Result.failure(preparedErrors);
        }
        Optional<MKStructureWorkspace> workspaceOpt = IMKStructureWorkspaceData.get(level)
                .getWorkspaceByAnchor(request.anchor());
        if (workspaceOpt.isEmpty()) {
            return Result.failure(List.of("no workspace exists at anchor " + request.anchor().toShortString()));
        }
        MKStructureWorkspace workspace = workspaceOpt.get();
        Optional<MKWorkspacePieceDefinition> hostPieceOpt = workspace.pieces().stream()
                .filter(piece -> piece.pieceId().equals(request.hostPieceId()))
                .findFirst();
        if (hostPieceOpt.isEmpty()) {
            return Result.failure(List.of("host template piece no longer exists"));
        }
        MKWorkspacePieceDefinition hostPiece = hostPieceOpt.get();
        if (!hostPiece.exportBounds().isInside(request.socketWorldPos())) {
            return Result.failure(List.of("socket position is outside the host authorial template"));
        }
        Optional<MKWorkspaceInsertFamilyDefinition> familyOpt = workspace.insertSlots().stream()
                .filter(family -> family.slotId().equals(request.familyId()))
                .filter(family -> family.kind() == MKWorkspaceInsertFamilyKind.INSERT_SOCKET)
                .findFirst();
        if (familyOpt.isEmpty()) {
            return Result.failure(List.of("insert slot " + request.familyId() + " does not exist"));
        }
        MKWorkspaceInsertFamilyDefinition family = familyOpt.get();
        MKWorkspaceInsertAttachmentFace templateFace =
                MKWorkspaceInsertAttachmentFace.fromDirection(request.socketFacing().getOpposite());
        if (!MKWorkspaceInsertTemplateCompatibility.hasAttachableTemplateJigsaw(level, workspace, family,
                request.socketFacing())) {
            return Result.failure(List.of("insert slot " + request.familyId() +
                    " has no template variant that can attach to a " +
                    request.socketFacing().getSerializedName() + "-facing socket"));
        }
        MKWorkspaceInsertAttachmentFace authoredFace = family.attachmentFace().orElse(templateFace);
        int faceUOffset = family.attachmentFace().isPresent() ? family.faceUOffset() :
                centeredUOffset(family.width(), family.depth(), templateFace);
        int faceVOffset = family.attachmentFace().isPresent() ? family.faceVOffset() :
                centeredVOffset(family.height(), family.depth(), templateFace);
        BoundingBox hostLocalBounds = new BoundingBox(0, 0, 0,
                hostPiece.exportBounds().getXSpan() - 1,
                hostPiece.exportBounds().getYSpan() - 1,
                hostPiece.exportBounds().getZSpan() - 1);
        List<String> errors = MKWorkspaceInsertSocketPlacement.validateOrientedFits(hostLocalBounds,
                request.socketWorldPos().subtract(hostPiece.worldOrigin()),
                family.width(), family.height(), family.depth(), authoredFace, faceUOffset, faceVOffset,
                templateFace, MKWorkspaceInsertTemplateCompatibility.jigsawOrientation(request.socketFacing()).top());
        if (errors.isEmpty()) {
            BoundingBox candidateBounds = MKWorkspaceInsertSocketPlacement.projectedOrientedInsertBounds(
                    request.socketWorldPos().subtract(hostPiece.worldOrigin()), family.width(), family.height(),
                    family.depth(), authoredFace, faceUOffset, faceVOffset, templateFace,
                    MKWorkspaceInsertTemplateCompatibility.jigsawOrientation(request.socketFacing()).top());
            errors = validateNoCollision(level, workspace, hostPiece, request.socketWorldPos(), candidateBounds);
        }
        if (!errors.isEmpty()) {
            return Result.failure(errors);
        }

        ResourceLocation insertPool = MKInsertSlotPools.poolId(workspace.namespace(), workspace.structureName(),
                family.slotId());
        placeHostSocketJigsaw(level, request.socketWorldPos(), request.socketFacing(), insertPool,
                safeFinalState(request.hostFinalState()));
        return Result.success(new MKWorkspaceInsertPlacementContext(
                workspace,
                hostPiece,
                request.socketWorldPos(),
                request.socketWorldPos().subtract(hostPiece.worldOrigin()),
                request.socketFacing(),
                safeFinalState(request.hostFinalState()),
                MKWorkspaceInsertTemplateCompatibility.compatibleFamilyIds(level, workspace, request.socketFacing()),
                footprintScanner.scan(level, workspace, hostPiece, request.socketWorldPos())
        ));
    }

    private List<String> validateNoCollision(ServerLevel level, MKStructureWorkspace workspace,
                                             MKWorkspacePieceDefinition hostPiece, BlockPos socketWorldPos,
                                             BoundingBox candidateBounds) {
        return footprintScanner.scan(level, workspace, hostPiece, socketWorldPos).stream()
                .filter(footprint -> MKWorkspaceInsertSocketPlacement.intersects(candidateBounds,
                        footprint.bounds()))
                .findFirst()
                .map(footprint -> List.of("insert footprint collides with existing insert " +
                        footprint.familyId() + " at " + footprint.socketLocalPos().toShortString()))
                .orElse(List.of());
    }

    private MKStructureWorkspace withInsertFamily(MKStructureWorkspace workspace,
                                                  MKWorkspaceInsertFamilyDefinition insertFamily) {
        ArrayList<MKWorkspaceInsertFamilyDefinition> insertFamilies = new ArrayList<>(workspace.insertSlots());
        insertFamilies.add(insertFamily);
        return new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.topologyProfile(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.verticalShellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                workspace.familyDefinitions(),
                workspace.openingProfiles(),
                workspace.linearRunFamilies(),
                insertFamilies,
                workspace.createdAt(),
                System.currentTimeMillis(),
                workspace.pieces(),
                workspace.layerStates()
        );
    }

    private void placeHostSocketJigsaw(ServerLevel level, BlockPos pos, Direction facing,
                                       ResourceLocation insertPool, String finalState) {
        placeJigsaw(level, pos, facing, insertBase(insertPool), insertAttach(insertPool), insertPool, finalState);
    }

    private void placeInsertTemplateJigsaw(ServerLevel level, MKWorkspacePieceDefinition templatePiece,
                                           MKWorkspaceInsertFamilyDefinition insertFamily,
                                           ResourceLocation insertPool) {
        MKWorkspaceInsertAttachmentFace attachmentFace = insertFamily.attachmentFace()
                .orElse(MKWorkspaceInsertAttachmentFace.BOTTOM);
        BlockPos localPos = MKWorkspaceInsertSocketPlacement.jigsawLocalPos(
                insertFamily.width(),
                insertFamily.height(),
                insertFamily.depth(),
                attachmentFace,
                insertFamily.faceUOffset(),
                insertFamily.faceVOffset()
        );
        placeJigsaw(level, templatePiece.worldOrigin().offset(localPos), attachmentFace.direction(),
                insertAttach(insertPool), insertBase(insertPool), EMPTY_POOL,
                insertFamily.templateJigsawFinalState());
    }

    private ResourceLocation insertBase(ResourceLocation insertPool) {
        return ResourceLocation.fromNamespaceAndPath(insertPool.getNamespace(), "base");
    }

    private ResourceLocation insertAttach(ResourceLocation insertPool) {
        return ResourceLocation.fromNamespaceAndPath(insertPool.getNamespace(), "attach");
    }

    private void placeJigsaw(ServerLevel level, BlockPos pos, Direction facing, ResourceLocation name,
                             ResourceLocation target, ResourceLocation pool, String finalState) {
        level.setBlock(pos, Blocks.JIGSAW.defaultBlockState()
                .setValue(JigsawBlock.ORIENTATION, MKWorkspaceInsertTemplateCompatibility.jigsawOrientation(facing)),
                Block.UPDATE_ALL);
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof JigsawBlockEntity jigsaw) {
            jigsaw.setName(name);
            jigsaw.setTarget(target);
            jigsaw.setPool(ResourceKey.create(Registries.TEMPLATE_POOL, pool));
            jigsaw.setFinalState(safeFinalState(finalState));
            jigsaw.setJoint(JigsawBlockEntity.JointType.ALIGNED);
            jigsaw.setChanged();
        }
    }

    private int centeredUOffset(int width, int depth, MKWorkspaceInsertAttachmentFace attachmentFace) {
        if (attachmentFace == MKWorkspaceInsertAttachmentFace.WEST ||
                attachmentFace == MKWorkspaceInsertAttachmentFace.EAST) {
            return depth / 2;
        }
        return width / 2;
    }

    private int centeredVOffset(int height, int depth, MKWorkspaceInsertAttachmentFace attachmentFace) {
        if (attachmentFace.isHorizontal()) {
            return 0;
        }
        return depth / 2;
    }

    private String safeFinalState(String value) {
        return value == null || value.isBlank() ? "minecraft:air" : value.trim();
    }
}
