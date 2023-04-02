package com.chaosbuffalo.mknpc.quest.rewards;

import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkcore.utils.WorldUtils;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mkweapons.items.randomization.LootConstructor;
import com.chaosbuffalo.mkweapons.items.randomization.LootTier;
import com.chaosbuffalo.mkweapons.items.randomization.LootTierManager;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlotManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.MutableComponent;

public class MKLootReward extends QuestReward {
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKNpc.MODID, "quest_reward.mk_loot");
    protected final ResourceLocationAttribute lootTier = new ResourceLocationAttribute("loot_tier",
            LootTierManager.INVALID_LOOT_TIER);
    protected final ResourceLocationAttribute lootSlot = new ResourceLocationAttribute("loot_slot",
            LootSlotManager.INVALID_LOOT_SLOT);

    public MKLootReward(ResourceLocation lootTier, ResourceLocation lootSlot, MutableComponent description) {
        super(TYPE_NAME, description);
        this.lootSlot.setValue(lootSlot);
        this.lootTier.setValue(lootTier);
        addAttributes(this.lootTier, this.lootSlot);
    }

    public MKLootReward() {
        super(TYPE_NAME, defaultDescription);
        addAttributes(this.lootTier, this.lootSlot);
    }

    @Override
    public void grantReward(Player player) {
        LootTier tier = LootTierManager.getTierFromName(lootTier.getValue());
        LootSlot slot = LootSlotManager.getSlotFromName(lootSlot.getValue());
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
