package com.chaosbuffalo.mknpc.init;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.*;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;

import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;


public class MKNpcWorldGen {

    public static final ResourceLocation UNKNOWN_PIECE = new ResourceLocation(MKNpc.MODID, "unknown_structure_piece");

    public static final DeferredRegister<StructureFeature<?>> STRUCTURE_REGISTRY = DeferredRegister.create(
            ForgeRegistries.STRUCTURE_FEATURES, MKNpc.MODID);
    public static final RegistryObject<StructureFeature<JigsawConfiguration>> TEST_JIGSAW = STRUCTURE_REGISTRY.register("test_jigsaw",
            () -> new MKJigsawStructure(JigsawConfiguration.CODEC, 0, true, true, (piece) -> true, false));

    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECE_REGISTRY = DeferredRegister.create(
            Registry.STRUCTURE_PIECE_REGISTRY, MKNpc.MODID);

    public static RegistryObject<StructurePieceType> MK_JIGSAW_PIECE_TYPE = STRUCTURE_PIECE_REGISTRY.register("mk_jigsaw", () -> MKPoolElementPiece::new);

    public static final DeferredRegister<StructurePoolElementType<?>> STRUCTURE_POOL_REGISTRY = DeferredRegister.create(
            Registry.STRUCTURE_POOL_ELEMENT_REGISTRY, MKNpc.MODID
    );

    public static class MKSingleJigsawWrapper implements StructurePoolElementType<MKSingleJigsawPiece> {

        @Override
        public Codec<MKSingleJigsawPiece> codec() {
            return MKSingleJigsawPiece.codec;
        }
    }

//    public static StructurePoolElementType<MKSingleJigsawPiece> _MK_SINGLE_JIGSAW_DESERIALIZER;
//
//    public static void registerStructurePoolTypes() {
//        _MK_SINGLE_JIGSAW_DESERIALIZER = StructurePoolElementType.register("mknpc:mk_single_jigsaw", MKSingleJigsawPiece.codec);
//    }

    public static final RegistryObject<StructurePoolElementType<MKSingleJigsawPiece>> MK_SINGLE_JIGSAW_DESERIALIZER = STRUCTURE_POOL_REGISTRY.register("mk_single_jigsaw",
            MKSingleJigsawWrapper::new);

    public static void register() {
        STRUCTURE_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
        STRUCTURE_PIECE_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
        STRUCTURE_POOL_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

}
