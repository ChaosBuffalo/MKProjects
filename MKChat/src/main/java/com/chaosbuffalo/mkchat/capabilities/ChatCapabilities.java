package com.chaosbuffalo.mkchat.capabilities;

import com.chaosbuffalo.mkchat.MKChat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = MKChat.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChatCapabilities {
    public static ResourceLocation PLAYER_DIALOGUE_CAP_ID = new ResourceLocation(MKChat.MODID,
            "player_dialogue_data");
    public static ResourceLocation NPC_DIALOGUE_CAP_ID = new ResourceLocation(MKChat.MODID,
            "npc_dialogue_data");


    public static final Capability<IPlayerDialogue> PLAYER_DIALOGUE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});


    public static final Capability<INpcDialogue> NPC_DIALOGUE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});


    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IPlayerDialogue.class);
        event.register(INpcDialogue.class);
    }


    public abstract static class Provider<CapTarget, CapType extends INBTSerializable<CompoundTag>> implements ICapabilitySerializable<CompoundTag> {

        private final CapType data;
        private final LazyOptional<CapType> capOpt;

        public Provider(CapTarget attached) {
            data = makeData(attached);
            capOpt = LazyOptional.of(() -> data);
        }

        abstract CapType makeData(CapTarget attached);

        abstract Capability<CapType> getCapability();

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return getCapability().orEmpty(cap, capOpt);
        }

        public void invalidate() {
            capOpt.invalidate();
        }

        @Override
        public CompoundTag serializeNBT() {
            return data.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            data.deserializeNBT(nbt);
        }
    }
}
