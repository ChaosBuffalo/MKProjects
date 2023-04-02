package com.chaosbuffalo.mknpc.world.gen.feature.structure.events.requirements;

import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.WorldStructureManager;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinitionManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class StructureHasNotableRequirement extends StructureEventRequirement{
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKNpc.MODID,
            "struct_requirement.has_notable");
    protected ResourceLocationAttribute npcDefinition = new ResourceLocationAttribute(
            "npcDefinition", NpcDefinitionManager.INVALID_NPC_DEF);

    public StructureHasNotableRequirement(ResourceLocation defName){
        this();
        npcDefinition.setValue(defName);

    }

    public StructureHasNotableRequirement(){
        super(TYPE_NAME);
        addAttribute(npcDefinition);
    }

    @Override
    public boolean meetsRequirements(MKStructureEntry entry,
                                     WorldStructureManager.ActiveStructure activeStructure, Level world) {
        return entry.hasNotableOfType(npcDefinition.getValue());
    }
}
