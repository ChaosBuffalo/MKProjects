package com.chaosbuffalo.mkcore.effects.triggers;

import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import com.chaosbuffalo.mkcore.events.ServerSideLeftClickEmpty;
import net.minecraft.world.entity.player.Player;

public class EmptyLeftClickTriggers extends SpellTriggers.EffectBasedTriggerCollection<EmptyLeftClickTriggers.Trigger> {
    @FunctionalInterface
    public interface Trigger {
        void apply(ServerSideLeftClickEmpty event, Player player, MKActiveEffect effect);
    }

    private static final String TAG = "EMPTY_LEFT_CLICK";

    public void onEmptyLeftClick(Player player, ServerSideLeftClickEmpty event) {
        runTrigger(player, TAG, (trigger, instance) -> trigger.apply(event, player, instance));
    }
}
