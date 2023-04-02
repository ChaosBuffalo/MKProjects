package com.chaosbuffalo.mknpc.world.gen.feature.structure.events.requirements;

import com.chaosbuffalo.mkcore.serialization.attributes.StringAttribute;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.WorldStructureManager;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class StructureHasPoiRequirement extends StructureEventRequirement{
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKNpc.MODID,
            "struct_requirement.has_poi");
    protected StringAttribute poiName = new StringAttribute("poiName", "invalid_default");

    public StructureHasPoiRequirement(String poiNameIn){
        this();
        poiName.setValue(poiNameIn);

    }

    public StructureHasPoiRequirement(){
        super(TYPE_NAME);
        addAttribute(poiName);
    }

    @Override
    public boolean meetsRequirements(MKStructureEntry entry,
                                     WorldStructureManager.ActiveStructure activeStructure, Level world) {
        return entry.hasPoi(poiName.getValue());
    }
}
