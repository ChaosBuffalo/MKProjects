package com.chaosbuffalo.mknpc.quest.objectives;

import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.quest.data.objective.ObjectiveInstanceData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public abstract class StructureInstanceObjective<T extends ObjectiveInstanceData> extends QuestObjective<T>{
    public final static ResourceLocation INVALID_OPTION = new ResourceLocation(MKNpc.MODID, "structure.invalid");

    protected final ResourceLocationAttribute structureName = new ResourceLocationAttribute("structure", INVALID_OPTION);
    protected final IntAttribute structureIndex = new IntAttribute("structureIndex", 0);

    public StructureInstanceObjective(ResourceLocation typeName, String name, ResourceLocation structure, MutableComponent... description) {
        this(typeName, name, description);
        structureName.setValue(structure);

    }

    public StructureInstanceObjective(ResourceLocation typeName, String name, ResourceLocation structure, int index, MutableComponent... description) {
        this(typeName, name, description);
        structureName.setValue(structure);
        structureIndex.setValue(index);

    }


    public int getStructureIndex(){
        return structureIndex.value();
    }

    public StructureInstanceObjective(ResourceLocation typeName, String name, MutableComponent... description){
        super(typeName, name, description);
        addAttributes(structureName, structureIndex);

    }

    public ResourceLocation getStructureName(){
        return structureName.getValue();
    }
}
