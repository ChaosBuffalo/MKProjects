package com.chaosbuffalo.mknpc.quest.rewards;

import com.chaosbuffalo.mkcore.utils.WorldUtils;
import com.chaosbuffalo.mkweapons.items.randomization.LootConstructor;
import com.chaosbuffalo.mkweapons.items.randomization.LootTier;
import com.chaosbuffalo.mkweapons.items.randomization.LootTierManager;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class MKLootReward extends QuestReward {
    public static final MapCodec<MKLootReward> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("loot_tier").forGetter(i -> i.lootTier),
            LootSlot.CODEC.fieldOf("loot_slot").forGetter(i -> i.lootSlot),
            ComponentSerialization.CODEC.fieldOf("description").forGetter(i -> i.description)
    ).apply(builder, MKLootReward::new));

    private final ResourceLocation lootTier;
    private final LootSlot lootSlot;
    private final Component description;

    public MKLootReward(ResourceLocation lootTier, LootSlot lootSlot, Component description) {
        this.lootTier = lootTier;
        this.lootSlot = lootSlot;
        this.description = description;
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
        LootTier tier = LootTierManager.getTierFromName(lootTier);
        if (tier != null) {
            LootConstructor constructor = tier.generateConstructorForSlot(player.getRandom(), lootSlot);
            if (constructor != null) {
                double diff = WorldUtils.getDifficultyForGlobalPos(GlobalPos.of(player.level().dimension(), player.blockPosition()));
                ItemStack loot = constructor.constructItem(player.getRandom(), diff);
                player.getInventory().placeItemBackInInventory(loot, true);
            }
        }
    }
}
