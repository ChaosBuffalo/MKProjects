package com.chaosbuffalo.mkfaction.event;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkfaction.MKFactionMod;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = MKFactionMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MKFactionRegistry {
    public static IForgeRegistry<MKFaction> FACTION_REGISTRY = null;

    public static final ResourceLocation FACTION_REGISTRY_NAME = new ResourceLocation(MKFactionMod.MODID, "abilities");

    @Nullable
    public static MKFaction getFaction(ResourceLocation name) {
        return FACTION_REGISTRY.getValue(name);
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void createRegistries(NewRegistryEvent event) {
        event.create(new RegistryBuilder<MKFaction>()
                .setName(FACTION_REGISTRY_NAME), r -> FACTION_REGISTRY = r);
    }
}
