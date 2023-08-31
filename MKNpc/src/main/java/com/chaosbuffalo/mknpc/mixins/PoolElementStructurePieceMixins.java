package com.chaosbuffalo.mknpc.mixins;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.IMKPoolElement;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.IMKPoolPiece;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.*;

import java.util.UUID;

@Mixin(PoolElementStructurePiece.class)
public abstract class PoolElementStructurePieceMixins implements IMKPoolPiece {

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
    @Unique
    protected UUID mknpc$instanceId;
    @Unique
    protected Structure mknpc$structure;


    @Override
    public void setContainingStructure(UUID instanceId, Structure structure) {
        this.mknpc$instanceId = instanceId;
        this.mknpc$structure = structure;
    }

    /**
     * @author kovak
     * @reason call our custom mkPlace function to pass structure metadata
     */
    @Overwrite
    public void place(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator,
                      RandomSource pRandom, BoundingBox pBox, BlockPos pPos, boolean pKeepJigsaws) {

        if (getElement() instanceof IMKPoolElement poolElement) {
            ResourceLocation name = pStructureManager.registryAccess().registryOrThrow(Registries.STRUCTURE).getKey(mknpc$structure);
            poolElement.mkPlace(structureTemplateManager, pLevel, pStructureManager, pGenerator,
                    position, pPos, rotation, pBox, pRandom, pKeepJigsaws, name, mknpc$instanceId);
        } else {
            getElement().place(structureTemplateManager, pLevel, pStructureManager, pGenerator, position, pPos, rotation,
                    pBox, pRandom, pKeepJigsaws);
        }
    }
}
