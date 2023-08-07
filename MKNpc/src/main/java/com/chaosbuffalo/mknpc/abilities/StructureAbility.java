package com.chaosbuffalo.mknpc.abilities;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.AbilityType;
import com.chaosbuffalo.mknpc.ContentDB;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

public abstract class StructureAbility extends MKAbility {

    @Override
    public AbilityType getType() {
        return AbilityType.Structure;
    }

    public Optional<MKStructureEntry> getStructure(LivingEntity caster) {
        return ContentDB.tryGetPrimaryData()
                .map(overworld -> MKNpc.getNpcData(caster)
                        .map(npcData -> npcData.getStructureId()
                                .flatMap(overworld::getStructureData))
                        .orElse(Optional.empty()))
                .orElse(Optional.empty());
    }


}
