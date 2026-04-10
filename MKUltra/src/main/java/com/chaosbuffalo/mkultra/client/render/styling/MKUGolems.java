package com.chaosbuffalo.mkultra.client.render.styling;


import com.chaosbuffalo.mknpc.client.render.models.styling.ModelLook;
import com.chaosbuffalo.mknpc.npc.NpcRegistries;
import com.chaosbuffalo.mkultra.MKUltra;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class MKUGolems {

    public static final ResourceLocation NECROTIDE_GOLEM = MKUltra.id("textures/entity/golem/necrotide_golem.png");

    public static final String NECROTIDE_GOLEM_NAME = "necrotide_golem";

    public static final ResourceKey<ModelLook> NECROTIDE_GOLEM_LOOK = lookKey(NECROTIDE_GOLEM_NAME);

    private static ResourceKey<ModelLook> lookKey(String lookName) {
        return ResourceKey.create(NpcRegistries.MODEL_LOOKS,
                MKUltra.id("golem/%s".formatted(lookName)));
    }
}
