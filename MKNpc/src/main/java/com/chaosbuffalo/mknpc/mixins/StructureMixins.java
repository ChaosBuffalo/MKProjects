package com.chaosbuffalo.mknpc.mixins;

import com.chaosbuffalo.mknpc.world.gen.IStructureStartMixin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;


@Mixin(StructureFeature.class)
public abstract class StructureMixins {


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

}

