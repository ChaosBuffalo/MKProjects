package com.chaosbuffalo.mkweapons.items.effects;

import com.chaosbuffalo.mkcore.serialization.IDynamicMapTypedSerializer;
import com.chaosbuffalo.mkcore.serialization.ISerializableAttributeContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public interface IItemEffect extends ISerializableAttributeContainer, IDynamicMapTypedSerializer {

    void addInformation(ItemStack stack, @Nullable Player player, List<Component> tooltip);

    default void onEntityEquip(LivingEntity entity) {
    }

    default void onEntityUnequip(LivingEntity entity) {
    }

    default void onSkillChange(Player player) {
    }

    default IItemEffect copy() {
        return ItemEffects.copyEffect(this);
    }


}
