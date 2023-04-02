package com.chaosbuffalo.mkweapons;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;

public class ClientUtils {

    public static float getClientSkillLevel(Attribute skill) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player != null) {
            return MKAbility.getSkillLevel(player, skill);
        }
        return 0.0f;
    }
}
