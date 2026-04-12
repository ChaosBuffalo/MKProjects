package com.chaosbuffalo.mkweapons;

import com.chaosbuffalo.mkcore.core.combat.MKMeleeManager;
import com.chaosbuffalo.mkcore.core.combat.MeleeSequenceTimingManager;
import com.chaosbuffalo.mkweapons.combat.ComboStrikeMeleeSequenceTimingResolver;
import com.chaosbuffalo.mkweapons.event.MKWeaponsEventHandler;
import com.chaosbuffalo.mkweapons.init.MKWeaponsCommands;
import com.chaosbuffalo.mkweapons.items.effects.IWeaponEffectsExtension;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import com.chaosbuffalo.mkweapons.items.weapon.types.WeaponTypeManager;
import com.chaosbuffalo.mkweapons.network.PacketHandler;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(MKWeapons.MODID)
public class MKWeapons {
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "mkweapons";
    public static final String REGISTER_MK_WEAPONS_EXTENSION = "register_mk_weapons_extension";
    public final WeaponTypeManager weaponTypeManager;

    public MKWeapons(IEventBus modBus) {
        NeoForge.EVENT_BUS.register(this);
        modBus.addListener(this::setup);
        modBus.addListener(this::processIMC);
        modBus.addListener(PacketHandler::register);
        MKWeaponsRegistry.setup(modBus);
        weaponTypeManager = new WeaponTypeManager();
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            MKWeaponsEventHandler.registerCombatTriggers();
            MeleeSequenceTimingManager.registerResolver(new ComboStrikeMeleeSequenceTimingResolver());
            MKMeleeManager.registerResolver(new com.chaosbuffalo.mkcore.core.combat.DualWieldResolver() {
                @Override
                public boolean canUseCustomMelee(net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.InteractionHand hand) {
                    return entity.getItemInHand(hand).getItem() instanceof IMKMeleeWeapon;
                }

                @Override
                public boolean canUseForAttack(net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.InteractionHand hand) {
                    if (entity.getItemInHand(hand).getItem() instanceof IMKMeleeWeapon weapon) {
                        return !weapon.getWeaponType().isTwoHanded();
                    }
                    return false;
                }
            });
        });
    }

    private void processIMC(final InterModProcessEvent event) {
        LOGGER.info("MKWeapons.processIMC");
        event.getIMCStream().forEach(m -> {
            if (m.method().equals(REGISTER_MK_WEAPONS_EXTENSION)) {
                LOGGER.info("IMC register weapon extensions from mod {} {}", m.senderModId(),
                        m.method());
                IWeaponEffectsExtension ext = (IWeaponEffectsExtension) m.messageSupplier().get();
                ext.registerWeaponEffectsExtension();
            }
        });
    }



    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        MKWeaponsCommands.registerCommands(event.getDispatcher());
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
