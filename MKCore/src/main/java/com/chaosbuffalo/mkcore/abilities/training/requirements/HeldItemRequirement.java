package com.chaosbuffalo.mkcore.abilities.training.requirements;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.training.AbilityTrainingRequirement;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class HeldItemRequirement extends AbilityTrainingRequirement {
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKCore.MOD_ID, "training_req.held_item");
    private Item item;
    private InteractionHand hand;

    public HeldItemRequirement(Item item, InteractionHand hand) {
        super(TYPE_NAME);
        this.item = item;
        this.hand = hand;
    }

    public <D> HeldItemRequirement(Dynamic<D> dynamic) {
        super(TYPE_NAME, dynamic);
    }

    @Override
    public boolean check(MKPlayerData playerData, MKAbility ability) {
        ItemStack stack = playerData.getEntity().getItemInHand(hand);
        if (stack.isEmpty())
            return false;

        return stack.getItem() == item;
    }

    @Override
    public void onLearned(MKPlayerData playerData, MKAbility ability) {

    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("item"), ops.createString(ForgeRegistries.ITEMS.getKey(item).toString()));
        builder.put(ops.createString("hand"), ops.createInt(hand.ordinal()));
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        dynamic.get("item").asString().result().ifPresent(x -> {
            this.item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(x));
        });
        this.hand = InteractionHand.values()[dynamic.get("hand").asInt(0)];

    }

    @Override
    public MutableComponent describe(MKPlayerData playerData) {
        String handName = hand == InteractionHand.MAIN_HAND ? "Main" : "Off";
        return new TextComponent("You must be holding a ")
                .append(new TranslatableComponent(item.getDescriptionId())) // Item.getName is client-only
                .append(new TextComponent(String.format(" in your %s hand", handName)));
    }
}
