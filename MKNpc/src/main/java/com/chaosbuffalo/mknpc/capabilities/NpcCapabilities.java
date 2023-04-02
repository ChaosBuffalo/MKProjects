package com.chaosbuffalo.mknpc.capabilities;

import com.chaosbuffalo.mknpc.MKNpc;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = MKNpc.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NpcCapabilities {
    public static ResourceLocation MK_NPC_CAP_ID = new ResourceLocation(MKNpc.MODID,
            "npc_data");
    public static ResourceLocation MK_WORLD_NPC_CAP_ID = new ResourceLocation(MKNpc.MODID,
            "world_npc_data");
    public static ResourceLocation MK_CHUNK_NPC_CAP_ID = new ResourceLocation(MKNpc.MODID,
            "chunk_npc_data");
    public static ResourceLocation MK_CHEST_CAP_ID = new ResourceLocation(MKNpc.MODID,
            "chest_npc_data");
    public static ResourceLocation MK_QUEST_CAP_ID = new ResourceLocation(MKNpc.MODID,
            "player_quest_data");

    public static final Capability<IEntityNpcData> ENTITY_NPC_DATA_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public static final Capability<IWorldNpcData> WORLD_NPC_DATA_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public static final Capability<IChunkNpcData> CHUNK_NPC_DATA_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public static final Capability<IChestNpcData> CHEST_NPC_DATA_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public static final Capability<IPlayerQuestingData> PLAYER_QUEST_DATA_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});


    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IEntityNpcData.class);
        event.register(IWorldNpcData.class);
        event.register(IChestNpcData.class);
        event.register(IChunkNpcData.class);
        event.register(IPlayerQuestingData.class);
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
