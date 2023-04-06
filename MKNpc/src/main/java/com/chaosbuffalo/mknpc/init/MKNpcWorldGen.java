package com.chaosbuffalo.mknpc.init;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawStructure;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKPoolElementPiece;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKSinglePoolElement;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;


public class MKNpcWorldGen {

    public static final ResourceLocation UNKNOWN_PIECE = new ResourceLocation(MKNpc.MODID, "unknown_structure_piece");

    public static final DeferredRegister<Structure> STRUCTURE_REGISTRY = DeferredRegister.create(
            Registries.STRUCTURE, MKNpc.MODID);
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPE_REGISTRY = DeferredRegister.create(
            Registries.STRUCTURE_TYPE, MKNpc.MODID);

    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECE_REGISTRY = DeferredRegister.create(
            Registries.STRUCTURE_PIECE, MKNpc.MODID);

    public static final DeferredRegister<StructurePoolElementType<?>> STRUCTURE_POOL_REGISTRY = DeferredRegister.create(
            Registries.STRUCTURE_POOL_ELEMENT, MKNpc.MODID);


    public static final RegistryObject<StructureType<MKJigsawStructure>> MK_STRUCTURE_TYPE = STRUCTURE_TYPE_REGISTRY.register("mk_jigsaw",
            () -> () -> MKJigsawStructure.CODEC);

    public static RegistryObject<StructurePieceType> MK_JIGSAW_PIECE_TYPE = STRUCTURE_PIECE_REGISTRY.register("mk_jigsaw",
            () -> MKPoolElementPiece::new);

    public static final RegistryObject<StructurePoolElementType<MKSinglePoolElement>> MK_SINGLE_JIGSAW_DESERIALIZER =
            STRUCTURE_POOL_REGISTRY.register("mk_single_jigsaw", () -> () -> MKSinglePoolElement.codec);

    public static void register(IEventBus modBus) {
        STRUCTURE_REGISTRY.register(modBus);
        STRUCTURE_PIECE_REGISTRY.register(modBus);
        STRUCTURE_POOL_REGISTRY.register(modBus);
        STRUCTURE_TYPE_REGISTRY.register(modBus);
    }

}
