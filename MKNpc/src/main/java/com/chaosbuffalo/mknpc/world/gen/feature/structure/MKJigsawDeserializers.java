package com.chaosbuffalo.mknpc.world.gen.feature.structure;


import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;

public class MKJigsawDeserializers {

    public static final StructurePoolElementType<MKSingleJigsawPiece> MK_SINGLE_JIGSAW_DESERIALIZER = StructurePoolElementType.
            register("mk_single_jigsaw", MKSingleJigsawPiece.codec);
}
