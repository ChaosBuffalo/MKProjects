package com.chaosbuffalo.mknpc.quest.rewards;

import com.chaosbuffalo.mkcore.utils.WorldUtils;
import com.chaosbuffalo.mkweapons.items.randomization.LootConstructor;
import com.chaosbuffalo.mkweapons.items.randomization.LootTier;
import com.chaosbuffalo.mkweapons.items.randomization.LootTierManager;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlotManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class MKLootReward extends QuestReward {
    public static final Codec<MKLootReward> CODEC = RecordCodecBuilder.<MKLootReward>mapCodec(builder ->
            builder.group(
                    ResourceLocation.CODEC.fieldOf("loot_tier").forGetter(i -> i.lootTier),
                    ResourceLocation.CODEC.fieldOf("loot_slot").forGetter(i -> i.lootSlot),
                    ExtraCodecs.COMPONENT.fieldOf("description").forGetter(i -> i.description)
            ).apply(builder, MKLootReward::new)
    ).codec();

    private final ResourceLocation lootTier;
    private final ResourceLocation lootSlot;
    private final Component description;

    public MKLootReward(ResourceLocation lootTier, ResourceLocation lootSlot, Component description) {
        this.lootTier = lootTier;
        this.lootSlot = lootSlot;
        this.description = description;
    }

    public MKLootReward(ResourceLocation lootTier, LootSlot lootSlot, Component description) {
        this(lootTier, lootSlot.getName(), description);
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
        LootSlot slot = LootSlotManager.getSlotFromName(lootSlot);
        if (tier != null && slot != null) {
            LootConstructor constructor = tier.generateConstructorForSlot(player.getRandom(), slot);
            if (constructor != null) {
                ItemStack loot = constructor.constructItem(player.getRandom(), WorldUtils.getDifficultyForGlobalPos(
                        GlobalPos.of(player.getCommandSenderWorld().dimension(), player.blockPosition())));
                player.getInventory().placeItemBackInInventory(loot, true);
            }
        }
    }
}
