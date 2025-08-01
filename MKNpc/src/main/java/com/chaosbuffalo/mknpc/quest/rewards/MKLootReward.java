package com.chaosbuffalo.mknpc.quest.rewards;

import com.chaosbuffalo.mkcore.utils.WorldUtils;
import com.chaosbuffalo.mkweapons.items.randomization.LootConstructor;
import com.chaosbuffalo.mkweapons.items.randomization.LootTier;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class MKLootReward extends QuestReward {
    public static final MapCodec<MKLootReward> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            LootTier.REFERENCE_CODEC.fieldOf("loot_tier").forGetter(i -> i.lootTier),
            LootSlot.CODEC.fieldOf("loot_slot").forGetter(i -> i.lootSlot),
            ComponentSerialization.CODEC.fieldOf("description").forGetter(i -> i.description)
    ).apply(builder, MKLootReward::new));

    private final Holder<LootTier> lootTier;
    private final LootSlot lootSlot;
    private final Component description;

    public MKLootReward(Holder<LootTier> lootTier, LootSlot lootSlot, Component description) {
        this.lootTier = lootTier;
        this.lootSlot = lootSlot;
        this.description = description;
    }

    public MKLootReward(Holder<LootTier> lootTier, LootSlot lootSlot, Holder<Item> rewardItem) {
        this.lootTier = lootTier;
        this.lootSlot = lootSlot;
        this.description = Component.translatable("mkultra.quest_reward.receive_item.name", rewardItem.value().getDescription());
    }

    @Override
    public QuestRewardType<? extends QuestReward> getType() {
        return QuestRewardTypes.MK_LOOT_REWARD.get();
    }

    @Override
    public Component getDescription() {
        return description;
    }

    @Override
    public void grantReward(Player player) {
        LootTier tier = lootTier.value();
        LootConstructor constructor = tier.generateConstructorForSlot(player.getRandom(), lootSlot);
        if (constructor != null) {
            double diff = WorldUtils.getDifficultyForGlobalPos(player.level(), GlobalPos.of(player.level().dimension(), player.blockPosition()));
            ItemStack loot = constructor.constructItem(player.getRandom(), diff);
            player.getInventory().placeItemBackInInventory(loot, true);
        }
    }
}
