package com.chaosbuffalo.mknpc.mixins;

import com.chaosbuffalo.mknpc.world.gen.IStructureStartMixin;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.IMKPoolPiece;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.UUID;

@Mixin(StructureStart.class)
public abstract class StructureStartMixins implements IStructureStartMixin {

    @Shadow
    @Final
    private PiecesContainer pieceContainer;
    @Shadow @Final private Structure structure;
    @Unique
    protected UUID mknpc$instanceId;

    @Inject(method = "<init>(Lnet/minecraft/world/level/levelgen/structure/Structure;Lnet/minecraft/world/level/ChunkPos;ILnet/minecraft/world/level/levelgen/structure/pieces/PiecesContainer;)V",
            at = @At("RETURN"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    protected void proxyInit(Structure structure, ChunkPos chunkPos, int references,
                             PiecesContainer piecesContainer, CallbackInfo ci) {
        mknpc$instanceId = UUID.randomUUID();
        setStartDataForPieces(piecesContainer);
    }

    private void setStartDataForPieces(PiecesContainer pieces) {
        for (StructurePiece piece : pieces.pieces()) {
            if (piece instanceof IMKPoolPiece poolPiece) {
                poolPiece.setContainingStructure(mknpc$instanceId, structure);
            }
        }
    }

    @Inject(method = "createTag(Lnet/minecraft/world/level/levelgen/structure/pieces/StructurePieceSerializationContext;Lnet/minecraft/world/level/ChunkPos;)Lnet/minecraft/nbt/CompoundTag;",
            at = @At(target = "Lnet/minecraft/nbt/CompoundTag;put(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;", value = "INVOKE"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void createTag(StructurePieceSerializationContext p_192661_, ChunkPos p_192662_, CallbackInfoReturnable<CompoundTag> cir, CompoundTag compoundtag) {
        compoundtag.putUUID("instanceId", mknpc$instanceId);
    }


    @Inject(method = "loadStaticStart(Lnet/minecraft/world/level/levelgen/structure/pieces/StructurePieceSerializationContext;Lnet/minecraft/nbt/CompoundTag;J)Lnet/minecraft/world/level/levelgen/structure/StructureStart;",
            at = @At("RETURN"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private static void loadStaticStart(StructurePieceSerializationContext piecescontainer, CompoundTag tag, long chunkpos, CallbackInfoReturnable<StructureStart> cir, String s) {
        if (cir.getReturnValue() != null && cir.getReturnValue().isValid()) {
            if ((Object) cir.getReturnValue() instanceof IStructureStartMixin) {
                ((IStructureStartMixin) (Object) cir.getReturnValue()).loadAdditional(tag);
            }
        }
    }

    @Override
    public UUID getInstanceId() {
        return mknpc$instanceId;
    }

    @Override
    public void loadAdditional(CompoundTag tag) {
        mknpc$instanceId = tag.getUUID("instanceId");
        setStartDataForPieces(pieceContainer);
    }
}
