package com.chaosbuffalo.mkcore.abilities.training.requirements;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.training.AbilityTrainingRequirement;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class HeldItemRequirement extends AbilityTrainingRequirement {
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKCore.MOD_ID, "training_req.held_item");
    public static final Codec<HeldItemRequirement> CODEC = RecordCodecBuilder.<HeldItemRequirement>mapCodec(builder -> {
        return builder.group(
                ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter(i -> i.item),
                Codec.STRING.xmap(InteractionHand::valueOf, InteractionHand::name).fieldOf("hand").forGetter(i -> i.hand)
        ).apply(builder, HeldItemRequirement::new);
    }).codec();

    private final Item item;
    private final InteractionHand hand;

    public HeldItemRequirement(Item item, InteractionHand hand) {
        super(TYPE_NAME);
        this.item = item;
        this.hand = hand;
    }

    @Override
    public boolean check(MKPlayerData playerData, MKAbility ability) {
        ItemStack stack = playerData.getEntity().getItemInHand(hand);
        if (stack.isEmpty())
            return false;

        return stack.is(item);
    }

    @Override
    public void onLearned(MKPlayerData playerData, MKAbility ability) {

    }

    @Override
    public MutableComponent describe(MKPlayerData playerData) {
        String handName = hand == InteractionHand.MAIN_HAND ? "Main" : "Off";
        return Component.literal("You must be holding a ")
                .append(Component.translatable(item.getDescriptionId())) // Item.getName is client-only
                .append(Component.literal(String.format(" in your %s hand", handName)));
    }
}
