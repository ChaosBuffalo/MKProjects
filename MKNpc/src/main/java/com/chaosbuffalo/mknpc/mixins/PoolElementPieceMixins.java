package com.chaosbuffalo.mknpc.mixins;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.IMKPoolElement;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.IMKPoolPiece;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

@Mixin(PoolElementStructurePiece.class)
public abstract class PoolElementPieceMixins implements IMKPoolPiece {

    @Shadow
    public abstract StructurePoolElement getElement();

    @Shadow
    @Final
    private StructureTemplateManager structureTemplateManager;
    @Shadow
    protected BlockPos position;
    @Shadow
    @Final
    protected Rotation rotation;
    protected UUID instanceId;
    protected ResourceLocation structureName;


    @Override
    public void setStart(UUID instanceId, ResourceLocation structureName) {
        this.instanceId = instanceId;
        this.structureName = structureName;
    }

    /**
     * @author kovak
     */
    @Overwrite
    public void place(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator,
                      RandomSource pRandom, BoundingBox pBox, BlockPos pPos, boolean pKeepJigsaws) {

        if (getElement() instanceof IMKPoolElement poolElement) {
            poolElement.mkPlace(structureTemplateManager, pLevel, pStructureManager, pGenerator,
                    position, pPos, rotation, pBox, pRandom, pKeepJigsaws, structureName, instanceId);
        } else {
            getElement().place(structureTemplateManager, pLevel, pStructureManager, pGenerator, position, pPos, rotation,
                    pBox, pRandom, pKeepJigsaws);
        }
    }
}
