package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkwidgets.utils.ManualAtlas;

public class GuiTextures {

    public static final ManualAtlas CORE_TEXTURES = new ManualAtlas(MKCore.makeRL("textures/gui/mkcore_ui.png"),
            1024, 1024);
    public static final String MANA_REGION = "mana";
    public static final String CAST_BAR_REGION = "cast_bar";
    public static final String ABILITY_BAR_REG = "ability_bar_regular";
    public static final String ABILITY_BAR_ULT = "ability_bar_ultimate";
    public static final String ABILITY_SLOT_REG_LOCKED = "ability_slot_reg_locked";
    public static final String ABILITY_SLOT_ULT_LOCKED = "ability_slot_ult_locked";
    public static final String ABILITY_SLOT_PASSIVE_LOCKED = "ability_slot_passive_locked";
    public static final String ABILITY_SLOT_REG = "ability_slot_reg";
    public static final String ABILITY_SLOT_ULT = "ability_slot_ult";
    public static final String ABILITY_SLOT_PASSIVE = "ability_slot_passive";
    public static final String BACKGROUND_320_240 = "background_320_240";
    public static final String DATA_BOX = "data_box";
    public static final String DATA_BOX_SHORT = "data_box_short";
    public static final String BACKGROUND_180_200 = "background_180_200";
    public static final String XP_BAR_BACKGROUND = "xp_background";
    public static final String XP_BAR_BLUE = "xp_bar_blue";
    public static final String XP_BAR_YELLOW = "xp_bar_yellow";
    public static final String POISE_BAR = "poise_bar";
    public static final String POISE_BREAK = "poise_break";
    public static final String HP_BAR = "hp_bar";
    public static final String MANA_BAR = "mana_bar";
    public static final String HP_WITHER_BAR = "hp_wither_bar";
    public static final String ABSORPTON_BAR = "absorption_bar";
    public static final String HP_BAR_LONG = "hp_bar_long";
    public static final String WITHER_BAR_LONG = "wither_bar_long";
    public static final String MANA_BAR_LONG = "mana_bar_long";
    public static final String ABSORPTION_BAR_LONG = "absorption_bar_long";
    public static final String XP_BAR_ON_SCREEN_YELLOW = "xp_bar_on_screen_yellow";
    public static final String XP_BAR_ON_SCREEN_BACKGROUND = "xp_bar_on_screen_background";
    public static final String XP_BAR_ON_SCREEN_BLUE = "xp_bar_on_screen_blue";
    public static final String PLAYER_BAR_OUTLINE = "player_bar_outline";
    public static final String SHORT_BAR_OUTLINE = "short_bar_outline";


    static {
        CORE_TEXTURES.addTextureRegion(MANA_REGION, 326, 51, 3, 8);
        CORE_TEXTURES.addTextureRegion(CAST_BAR_REGION, 326, 45, 50, 3);
        CORE_TEXTURES.addTextureRegion(ABILITY_BAR_ULT, 392, 0, 20, 20);
        CORE_TEXTURES.addTextureRegion(ABILITY_BAR_REG, 392, 22, 20, 20);
        CORE_TEXTURES.addTextureRegion(ABILITY_SLOT_REG, 326, 0, 20, 20);
        CORE_TEXTURES.addTextureRegion(ABILITY_SLOT_REG_LOCKED, 326, 22, 20, 20);
        CORE_TEXTURES.addTextureRegion(ABILITY_SLOT_ULT, 348, 0, 20, 20);
        CORE_TEXTURES.addTextureRegion(ABILITY_SLOT_ULT_LOCKED, 348, 22, 20, 20);
        CORE_TEXTURES.addTextureRegion(ABILITY_SLOT_PASSIVE, 370, 0, 20, 20);
        CORE_TEXTURES.addTextureRegion(ABILITY_SLOT_PASSIVE_LOCKED, 370, 22, 20, 20);
        CORE_TEXTURES.addTextureRegion(BACKGROUND_320_240, 0, 0, 320, 240);
        CORE_TEXTURES.addTextureRegion(DATA_BOX, 0, 245, 308, 155);
        CORE_TEXTURES.addTextureRegion(BACKGROUND_180_200, 310, 245, 180, 200);
        CORE_TEXTURES.addTextureRegion(XP_BAR_BACKGROUND, 326, 74, 67, 11);
        CORE_TEXTURES.addTextureRegion(XP_BAR_BLUE, 330, 89, 59, 11);
        CORE_TEXTURES.addTextureRegion(XP_BAR_YELLOW, 330, 105, 59, 11);
        CORE_TEXTURES.addTextureRegion(DATA_BOX_SHORT, 0, 404, 308, 133);
        CORE_TEXTURES.addTextureRegion(POISE_BAR, 326, 118, 50, 3);
        CORE_TEXTURES.addTextureRegion(POISE_BREAK, 326, 125, 50, 3);
        CORE_TEXTURES.addTextureRegion(HP_BAR, 326, 61, 50, 3);
        CORE_TEXTURES.addTextureRegion(MANA_BAR, 326, 66, 50, 3);
        CORE_TEXTURES.addTextureRegion(HP_WITHER_BAR, 326, 70, 50, 3);
        CORE_TEXTURES.addTextureRegion(CAST_BAR_REGION, 326, 45, 50, 5);
        CORE_TEXTURES.addTextureRegion(HP_BAR_LONG, 378, 61, 75, 3);
        CORE_TEXTURES.addTextureRegion(MANA_BAR_LONG, 378, 66, 75, 3);
        CORE_TEXTURES.addTextureRegion(WITHER_BAR_LONG, 378, 70, 75, 3);
        CORE_TEXTURES.addTextureRegion(ABSORPTION_BAR_LONG, 378, 54, 75, 5);
        CORE_TEXTURES.addTextureRegion(XP_BAR_ON_SCREEN_YELLOW, 393, 108, 59, 5);
        CORE_TEXTURES.addTextureRegion(XP_BAR_ON_SCREEN_BACKGROUND, 391, 100, 63, 5);
        CORE_TEXTURES.addTextureRegion(XP_BAR_ON_SCREEN_BLUE, 393, 92, 59, 5);
        CORE_TEXTURES.addTextureRegion(ABSORPTON_BAR, 378, 45, 50, 5);
        CORE_TEXTURES.addTextureRegion(PLAYER_BAR_OUTLINE, 455, 54, 75, 5);
        CORE_TEXTURES.addTextureRegion(SHORT_BAR_OUTLINE, 455, 45, 50, 5);


    }

}
