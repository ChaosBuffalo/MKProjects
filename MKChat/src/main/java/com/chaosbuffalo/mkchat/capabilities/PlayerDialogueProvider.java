package com.chaosbuffalo.mkchat.capabilities;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;

public class PlayerDialogueProvider extends ChatCapabilities.Provider<Player, IPlayerDialogue> {

    public PlayerDialogueProvider(Player player) {
        super(player);
    }

    @Override
    PlayerDialogueHandler makeData(Player attached) {
        return new PlayerDialogueHandler(attached);
    }

    @Override
    Capability<IPlayerDialogue> getCapability() {
        return ChatCapabilities.PLAYER_DIALOGUE_CAPABILITY;
    }
}
