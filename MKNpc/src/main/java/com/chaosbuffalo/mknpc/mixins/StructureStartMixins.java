package com.chaosbuffalo.mknpc.mixins;


import com.chaosbuffalo.mknpc.world.gen.IStructureStartMixin;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawStructure;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.IMKPoolPiece;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

@Mixin(StructureStart.class)
public abstract class StructureStartMixins implements IStructureStartMixin {

    @Shadow public abstract List<StructurePiece> getPieces();

    @Shadow @Final private PiecesContainer pieceContainer;
    protected UUID instanceId;
    @Nullable
    protected ResourceLocation structureName;

    @Inject(method = "<init>(Lnet/minecraft/world/level/levelgen/structure/Structure;Lnet/minecraft/world/level/ChunkPos;ILnet/minecraft/world/level/levelgen/structure/pieces/PiecesContainer;)V",
            at = @At("RETURN"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    protected void proxyInit(Structure feature, ChunkPos chunkPos, int references,
                             PiecesContainer piecesContainer, CallbackInfo ci) {
        instanceId = UUID.randomUUID();
        if (feature instanceof MKJigsawStructure) {
            structureName = ((MKJigsawStructure) feature).getStructureName();
            setStartDataForPieces(piecesContainer);
        }

    }

    private void setStartDataForPieces(PiecesContainer pieces) {
        for (StructurePiece piece : pieces.pieces()) {
            if (piece instanceof IMKPoolPiece) {
                ((IMKPoolPiece) piece).setStart(instanceId, structureName);
            }
        }
    }

    @Inject(method = "createTag(Lnet/minecraft/world/level/levelgen/structure/pieces/StructurePieceSerializationContext;Lnet/minecraft/world/level/ChunkPos;)Lnet/minecraft/nbt/CompoundTag;",
            at = @At(target = "Lnet/minecraft/nbt/CompoundTag;put(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;", value = "INVOKE"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void createTag(StructurePieceSerializationContext p_192661_, ChunkPos p_192662_, CallbackInfoReturnable<CompoundTag> cir, CompoundTag compoundtag) {
        compoundtag.putUUID("instanceId", instanceId);
        if (structureName != null){
            compoundtag.putString("structureName", structureName.toString());
        }
    }


    @Inject(method = "loadStaticStart(Lnet/minecraft/world/level/levelgen/structure/pieces/StructurePieceSerializationContext;Lnet/minecraft/nbt/CompoundTag;J)Lnet/minecraft/world/level/levelgen/structure/StructureStart;",
            at = @At("RETURN"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private static void loadStaticStart(StructurePieceSerializationContext piecescontainer, CompoundTag tag, long chunkpos, CallbackInfoReturnable<StructureStart> cir, String s) {
        if (cir.getReturnValue() != null) {
            if ((Object) cir.getReturnValue() instanceof IStructureStartMixin) {
                ((IStructureStartMixin) (Object) cir.getReturnValue()).loadAdditional(tag);
            }
        }
    }

    @Override
    public UUID getInstanceId() {
        return instanceId;
    }

    @Override
    public void loadAdditional(CompoundTag tag) {
        instanceId = tag.getUUID("instanceId");
        if (tag.contains("structureName")) {
            structureName = new ResourceLocation(tag.getString("structureName"));
            setStartDataForPieces(pieceContainer);
        }

    }
}
