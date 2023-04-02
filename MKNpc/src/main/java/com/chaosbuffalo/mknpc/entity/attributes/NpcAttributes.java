package com.chaosbuffalo.mknpc.entity.attributes;

import com.chaosbuffalo.mknpc.MKNpc;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class NpcAttributes {

        public static final RangedAttribute AGGRO_RANGE = (RangedAttribute) new RangedAttribute(
                "attribute.name.mk.aggro_range", 5, 0, 128)
                .setRegistryName(MKNpc.MODID, "aggro_range")
                .setSyncable(false);


}
