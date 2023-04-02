package com.chaosbuffalo.mknpc.mixins;


import com.chaosbuffalo.mknpc.world.gen.IStructureStartMixin;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKPoolElementPiece;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Random;
import java.util.UUID;

@Mixin(StructureStart.class)
public abstract class StructureStartMixins implements IStructureStartMixin {

    protected UUID instanceId;

    @Inject(method = "Lnet/minecraft/world/level/levelgen/structure/StructureStart;<init>(Lnet/minecraft/world/level/levelgen/feature/ConfiguredStructureFeature;Lnet/minecraft/world/level/ChunkPos;ILnet/minecraft/world/level/levelgen/structure/pieces/PiecesContainer;)V",
            at = @At("RETURN"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    protected void proxyInit(ConfiguredStructureFeature<?, ?> feature, ChunkPos chunkPos, int references,
                          PiecesContainer piecesContainer, CallbackInfo ci) {
        instanceId = UUID.randomUUID();
        for (StructurePiece piece : piecesContainer.pieces()) {
            if (piece instanceof MKPoolElementPiece) {
                ((MKPoolElementPiece) piece).setInstanceId(instanceId);
            }
        }
    }

    @Inject(method = "Lnet/minecraft/world/level/levelgen/structure/StructureStart;createTag(Lnet/minecraft/world/level/levelgen/structure/pieces/StructurePieceSerializationContext;Lnet/minecraft/world/level/ChunkPos;)Lnet/minecraft/nbt/CompoundTag;",
            at = @At(target = "Lnet/minecraft/nbt/CompoundTag;put(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;", value = "INVOKE"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void createTag(StructurePieceSerializationContext p_192661_, ChunkPos p_192662_, CallbackInfoReturnable<CompoundTag> cir, CompoundTag compoundtag) {
        compoundtag.putUUID("instanceId", instanceId);
    }

//    @Inject(method = "Lnet/minecraft/world/level/levelgen/structure/StructureStart;placeInChunk(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/StructureFeatureManager;Lnet/minecraft/world/level/chunk/ChunkGenerator;Ljava/util/Random;Lnet/minecraft/world/level/levelgen/structure/BoundingBox;Lnet/minecraft/world/level/ChunkPos;)V",
//        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/StructurePiece;postProcess(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/StructureFeatureManager;Lnet/minecraft/world/level/chunk/ChunkGenerator;Ljava/util/Random;Lnet/minecraft/world/level/levelgen/structure/BoundingBox;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/core/BlockPos;)V"),
//        locals = LocalCapture.CAPTURE_FAILHARD)
//    private void proxyPostProcess(WorldGenLevel p_73584_, StructureFeatureManager p_73585_, ChunkGenerator p_73586_, Random p_73587_, BoundingBox p_73588_, ChunkPos p_73589_, CallbackInfoReturnable<StructurePiece> cir, StructurePiece piece) {
//
//    }

    @Override
    public UUID getInstanceId() {
        return instanceId;
    }

    @Override
    public void loadAdditional(CompoundTag tag) {
        instanceId = tag.getUUID("instanceId");
    }
}
