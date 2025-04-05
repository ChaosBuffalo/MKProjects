package com.chaosbuffalo.mkultra.data.generators.npc;


import com.chaosbuffalo.mknpc.data.NpcDefinitionProvider;
import com.chaosbuffalo.mkultra.MKUltra;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;

import java.util.concurrent.CompletableFuture;

public class MKUNpcProvider extends NpcDefinitionProvider {

    public MKUNpcProvider(DataGenerator generator, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(generator, lookupProvider, MKUltra.MODID);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return CompletableFuture.allOf(
                writeDefinition(GreenKnightNpcs.generateGreenLady(), cache),
                writeDefinition(GreenKnightNpcs.generateGreenLadyGuard1(), cache),
                writeDefinition(GreenKnightNpcs.generateGreenLadyGuard2(), cache),
                writeDefinition(GreenKnightNpcs.generateGreenSmith(), cache),
                writeDefinition(HyboreanNpcs.generateHyboreanWarrior(), cache),
                writeDefinition(HyboreanNpcs.generateHyboreanHonorGuard(), cache),
                writeDefinition(HyboreanNpcs.generateHyboreanArcher(), cache),
                writeDefinition(HyboreanNpcs.generateHyboreanSorcerer(), cache),
                writeDefinition(HyboreanNpcs.generateAncientKing(), cache),
                writeDefinition(HyboreanNpcs.generateHyboreanSorcererQueen(), cache),
                writeDefinition(IntroCastleNpcs.generateCrumblingTrooper(), cache),
                writeDefinition(IntroCastleNpcs.generateCrumblingTrooperMage(), cache),
                writeDefinition(IntroCastleNpcs.generateDecayingZombieArcher(), cache),
                writeDefinition(IntroCastleNpcs.generateDecayingZombiePiglin(), cache),
                writeDefinition(IntroCastleNpcs.generateImperialMagus(), cache),
                writeDefinition(IntroCastleNpcs.generateTrooperCaptain(), cache),
                writeDefinition(IntroCastleNpcs.generateTrooperExecution(), cache),
                writeDefinition(IntroCastleNpcs.generateSkeletalTrooperMage(), cache),
                writeDefinition(IntroCastleNpcs.generateBurningSkeleton(), cache),
                writeDefinition(IntroCastleNpcs.generateClericAcolyte(), cache),
                writeDefinition(IntroCastleNpcs.generateClericApprentice(), cache),
                writeDefinition(IntroCastleNpcs.generateForlornGhost(), cache),
                writeDefinition(IntroCastleNpcs.generateNetherMageInitiate(), cache),
                writeDefinition(ClericNpcs.generateTempleGuard(), cache),
                writeDefinition(ClericNpcs.generateTempleGuard2(), cache),
                writeDefinition(ClericNpcs.generateCleric(), cache),
                writeDefinition(NecrotideNpcs.generateNecrotideCultistAcolyte(), cache),
                writeDefinition(NecrotideNpcs.generateNecrotideCultist(), cache),
                writeDefinition(NecrotideNpcs.generateSkeletalLock(), cache),
                writeDefinition(NecrotideNpcs.generateNecrotideGolem(), cache),
                writeDefinition(NecrotideNpcs.generateNecrotideSkeletalArcher(), cache),
                writeDefinition(NecrotideNpcs.generateNecrotideSkeletalWarrior(), cache),
                writeDefinition(SeawovenNpcs.generateSeawovenSkeleton(), cache),
                writeDefinition(SeawovenNpcs.generateSeawovenWretch(), cache),
                writeDefinition(DecayingChurchNpcs.generateAncientPriestGhost(), cache),
                writeDefinition(DecayingChurchNpcs.generateAncientCardinal(), cache),
                writeDefinition(DecayingChurchNpcs.generateGhostApprentice(), cache)
        );
    }


    @Override
    public String getName() {
        return "MKU NPC GEN";
    }
}
