package com.chaosbuffalo.mknpc.client.render.models.styling;

public class ModelStyles {

    public static final String HAIR_1 = "hair_1";
    public static final String HAIR_2 = "hair_2";
    public static final String CLOTHES = "clothes_1";

    public static final String LONG_HAIR_NAME = "long_hair";
    public static final String LONG_HAIR_ARMORED_NAME = "long_hair_armored";
    public static final String SHORT_HAIR_NAME = "short_hair";
    public static final String CLOTHES_ONLY_NAME = "clothes_only";
    public static final String BASIC_NAME = "basic";
    public static final String CLOTHES_ARMOR_NAME = "clothes_armor";
    public static final String CLOTHES_ARMOR_TRANSLUCENT_NAME = "clothes_armor_translucent";

    public static final ModelStyle ARMORED_LONG_HAIR_STYLE = new ModelStyle(
            LONG_HAIR_ARMORED_NAME,
            true,
            false,
            new LayerStyle(HAIR_1, 0.25f),
            new LayerStyle(CLOTHES, 0.4f),
            new LayerStyle(HAIR_2, 1.15f));

    public static final ModelStyle LONG_HAIR_STYLE = new ModelStyle(
            LONG_HAIR_NAME,
            true,
            true,
            new LayerStyle(HAIR_1, 0.25f),
            new LayerStyle(CLOTHES, 0.4f),
            new LayerStyle(HAIR_2, 0.45f));

    public static final ModelStyle SHORT_HAIR_STYLE = new ModelStyle(
            SHORT_HAIR_NAME,
            true,
            false,
            new LayerStyle(HAIR_1, 0.25f),
            new LayerStyle(CLOTHES, 0.4f));

    public static final ModelStyle CLOTHES_ONLY_STYLE = new ModelStyle(
            CLOTHES_ONLY_NAME,
            true,
            false,
            new LayerStyle(CLOTHES,0.25f));

    public static final ModelStyle CLOTHES_ARMOR_STYLE = new ModelStyle(
            CLOTHES_ARMOR_NAME,
            true,
            false,
            new LayerStyle(CLOTHES,0.75f));

    public static final ModelStyle CLOTHES_ARMOR_TRANSLUCENT_STYLE = new ModelStyle(
            CLOTHES_ARMOR_TRANSLUCENT_NAME,
            true,
            false,
            new LayerStyle(CLOTHES,0.75f, true));

    public static final ModelStyle BASIC_STYLE = new ModelStyle(
            BASIC_NAME,
            true,
            false);
}
