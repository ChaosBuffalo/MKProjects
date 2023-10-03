package com.chaosbuffalo.mkchat.capabilities;

import com.chaosbuffalo.mkchat.MKChat;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public class ChatCapabilities {
    public static ResourceLocation PLAYER_DIALOGUE_CAP_ID = new ResourceLocation(MKChat.MODID,
            "player_dialogue_data");
    public static ResourceLocation NPC_DIALOGUE_CAP_ID = new ResourceLocation(MKChat.MODID,
            "npc_dialogue_data");


    public static final Capability<IPlayerDialogue> PLAYER_DIALOGUE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });


    public static final Capability<INpcDialogue> NPC_DIALOGUE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });


    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IPlayerDialogue.class);
        event.register(INpcDialogue.class);
    }
}
