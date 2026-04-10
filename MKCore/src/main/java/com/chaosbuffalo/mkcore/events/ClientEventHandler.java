package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKConfig;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.client.gui.IPlayerDataAwareScreen;
import com.chaosbuffalo.mkcore.client.gui.ParticleEditorScreen;
import com.chaosbuffalo.mkcore.client.gui.PlayerPageRegistry;
import com.chaosbuffalo.mkcore.client.rendering.MKPlayerRenderer;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;
import com.chaosbuffalo.mkcore.init.CoreEffects;
import com.chaosbuffalo.mkcore.item.ArmorClass;
import com.chaosbuffalo.mkcore.item.CoreItemComponents;
import com.chaosbuffalo.mkcore.item.ItemGrantedAbility;
import com.chaosbuffalo.mkcore.network.ExecuteActiveAbilityPacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.targeting_api.Targeting;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;


@EventBusSubscriber(modid = MKCore.MOD_ID, value = Dist.CLIENT)
public class ClientEventHandler {

    private static final KeyMapping playerMenuBind = new KeyMapping("key.hud.playermenu",
            InputConstants.KEY_J, "key.mkcore.category");
    private static final KeyMapping particleEditorBind = new KeyMapping("key.hud.particle_editor",
            InputConstants.KEY_ADD, "key.mkcore.category");
    private static KeyMapping[] activeAbilityBinds;
    private static KeyMapping[] ultimateAbilityBinds;
    private static KeyMapping itemAbilityBind;

    @EventBusSubscriber(modid = MKCore.MOD_ID, value = Dist.CLIENT)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerKeyBinding(RegisterKeyMappingsEvent event) {
            event.register(playerMenuBind);
            event.register(particleEditorBind);

            activeAbilityBinds = new KeyMapping[GameConstants.MAX_BASIC_ABILITIES];
            for (int i = 0; i < GameConstants.MAX_BASIC_ABILITIES; i++) {
                String bindName = String.format("key.hud.active_ability%d", i + 1);
                int key = InputConstants.KEY_1 + i;
                KeyMapping bind = new KeyMapping(bindName, KeyConflictContext.IN_GAME, KeyModifier.ALT,
                        InputConstants.getKey(key, 0), "key.mkcore.abilitybar");

                event.register(bind);
                activeAbilityBinds[i] = bind;
            }

            ultimateAbilityBinds = new KeyMapping[GameConstants.MAX_ULTIMATE_ABILITIES];
            for (int i = 0; i < GameConstants.MAX_ULTIMATE_ABILITIES; i++) {
                String bindName = String.format("key.hud.ultimate_ability%d", i + 1);
                int key = InputConstants.KEY_6 + i;
                KeyMapping bind = new KeyMapping(bindName, KeyConflictContext.IN_GAME, KeyModifier.ALT,
                        InputConstants.getKey(key, 0), "key.mkcore.abilitybar");

                event.register(bind);
                ultimateAbilityBinds[i] = bind;
            }


            int defaultItemKey = InputConstants.KEY_8;
            itemAbilityBind = new KeyMapping("key.hud.item_ability", KeyConflictContext.IN_GAME, KeyModifier.ALT,
                    InputConstants.getKey(defaultItemKey, 0), "key.mkcore.abilitybar");
            event.register(itemAbilityBind);
        }
    }

    public static float getTotalGlobalCooldown() {
        return (float) GameConstants.GLOBAL_COOLDOWN_TICKS / GameConstants.TICKS_PER_SECOND;
    }

    @SubscribeEvent
    public static void onKeyEvent(InputEvent.Key event) {
        handleInputEvent();
    }

    @SubscribeEvent
    public static void onMouseEvent(InputEvent.MouseButton.Post event) {
        handleInputEvent();
    }

    @SubscribeEvent
    public static void onPreMouseEvent(InputEvent.MouseButton.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        MKCore.getEntityData(minecraft.player).ifPresent(playerData -> {
            if (playerData.getEffects().isEffectActive(CoreEffects.STUN.get()) &&
                    minecraft.screen == null) {
                event.setCanceled(true);
            }
        });
    }

    @SubscribeEvent
    public static void cancelHealth(RenderGuiLayerEvent.Pre event) {
        if (event.getName().equals(VanillaGuiLayers.PLAYER_HEALTH)) {
            // Make room for our health and mana bars
            Minecraft.getInstance().gui.leftHeight += 12;
            event.setCanceled(true);
        }
    }

    static void handleAbilityBarPressed(MKPlayerData player, AbilityGroupId group, int slot) {
        if (player.getAbilityExecutor().isOnGlobalCooldown() ||
                player.getEffects().isEffectActive(CoreEffects.STUN.get()))
            return;

        if (player.getAbilityExecutor().clientSimulateAbility(group, slot)) {
//            MKCore.LOGGER.debug("sending execute ability {} {}", group, slot);
            PacketHandler.sendMessageToServer(new ExecuteActiveAbilityPacket(group, slot));
            player.getAbilityExecutor().startGlobalCooldown();
        }
    }

    public static void handleInputEvent() {
        Player player = Minecraft.getInstance().player;
        if (player == null)
            return;

        MKPlayerData playerData = MKCore.getPlayerOrThrow(player);

        while (playerMenuBind.consumeClick()) {
            PlayerPageRegistry.openDefaultPlayerScreen(playerData);
        }

        while (particleEditorBind.consumeClick()) {
            Minecraft.getInstance().setScreen(new ParticleEditorScreen());
        }

        for (int i = 0; i < activeAbilityBinds.length; i++) {
            KeyMapping bind = activeAbilityBinds[i];
            while (bind.consumeClick()) {
                handleAbilityBarPressed(playerData, AbilityGroupId.Basic, i);
            }
        }

        for (int i = 0; i < ultimateAbilityBinds.length; i++) {
            KeyMapping bind = ultimateAbilityBinds[i];
            while (bind.consumeClick()) {
                handleAbilityBarPressed(playerData, AbilityGroupId.Ultimate, i);
            }
        }

        while (itemAbilityBind.consumeClick()) {
            handleAbilityBarPressed(playerData, AbilityGroupId.Item, 0);
        }
    }

    @SubscribeEvent
    public static void onPlayerDataUpdated(PlayerDataEvent.Updated event) {
        if (event.getPlayer().getCommandSenderWorld().isClientSide) {
            Player local = Minecraft.getInstance().player;
            if (local == null || !event.getPlayer().is(local))
                return;

            if (Minecraft.getInstance().screen instanceof IPlayerDataAwareScreen screen) {
                screen.onPlayerDataUpdate();
            }
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && event.getHand() == InteractionHand.MAIN_HAND) {
            var renderer = mc.getEntityRenderDispatcher().getRenderer(mc.player);
            if (renderer instanceof MKPlayerRenderer playerRenderer) {
                playerRenderer.renderHandFirstPerson(mc.player);
            }
        }
    }

    @SubscribeEvent
    public static void addCustomTooltipItems(ItemTooltipEvent event) {
        // Don't do anything during the initial search tree population
        if (event.getEntity() == null)
            return;

        addArmorClassTooltip(event);
        addGrantedAbilityTooltip(event.getItemStack(), event.getToolTip());
    }

    private static void addGrantedAbilityTooltip(ItemStack stack, List<Component> tooltip) {
        ItemGrantedAbility itemAbility = stack.get(CoreItemComponents.ITEM_ABILITY);
        if (itemAbility != null) {
            MKAbility ability = itemAbility.ability().value();
            tooltip.add(Component.translatable("mkcore.item_tooltip.grants_ability",
                    ability.getAbilityName()).withStyle(ChatFormatting.GOLD));
        }
    }

    private static void addArmorClassTooltip(ItemTooltipEvent event) {
        if (!MKConfig.CLIENT.showArmorClassOnTooltip.get() || event.getEntity() == null)
            return;

        if (event.getItemStack().getItem() instanceof ArmorItem armorItem) {
            Holder<ArmorClass> holder = ArmorClass.getHolder(event.getItemStack());
            if (holder == null) {
                return;
            }

            ArmorClass armorClass = holder.value();
            event.getToolTip().add(Component.translatable("mkcore.gui.item.armor_class.name")
                    .append(": ")
                    .append(armorClass.getName())
                    .withStyle(ChatFormatting.GRAY));

            if (MKConfig.CLIENT.showArmorClassEffectsOnTooltip.get()) {
                List<Component> tooltip = event.getToolTip();
                if (event.getFlags().hasShiftDown()) {
                    var equip = MKCore.getPlayerOrThrow(event.getEntity()).getEquipment();
                    armorClass.getPositiveModifierMap(armorItem.getEquipmentSlot())
                            .forEach(((attribute, modifier) -> addArmorClassAttributeToTooltip(tooltip, attribute, modifier, ChatFormatting.GREEN, event.getFlags())));
                    if (!equip.isArmorClassMastered(holder)) {
                        armorClass.getNegativeModifierMap(armorItem.getEquipmentSlot())
                                .forEach(((attribute, modifier) -> addArmorClassAttributeToTooltip(tooltip, attribute, modifier, ChatFormatting.RED, event.getFlags())));
                    }
                } else {
                    tooltip.add(Component.translatable("mkcore.gui.item.armor_class.effect_prompt").withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }
    }

    private static void addArmorClassAttributeToTooltip(List<Component> tooltip, Holder<Attribute> attribute,
                                                        AttributeModifier modifier, ChatFormatting color, TooltipFlag flag) {
        Component component = Component.translatable("mkcore.gui.item.armor_class.effect.name")
                .withStyle(color)
                .append(attribute.value().toComponent(modifier, flag));

        tooltip.add(component);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAttackReplacement(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isAttack() && event.getHand() == InteractionHand.MAIN_HAND) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null) {
                return;
            }

            if (mc.crosshairPickEntity != null &&
                    MKConfig.CLIENT.disableAutoattackForFriend.get() &&
                    Targeting.isValidFriendly(player, mc.crosshairPickEntity)) {
                event.setSwingHand(false);
                event.setCanceled(true);
                return;
            }

            var combat = MKCore.getPlayerOrThrow(player).getCombatExtension();
            if (combat.shouldDelayPrimaryAttack() && mc.crosshairPickEntity != null) {
                combat.queuePrimaryAttack(mc.crosshairPickEntity);
                player.connection.send(ServerboundInteractPacket.createAttackPacket(mc.crosshairPickEntity, player.isShiftKeyDown()));
                event.setSwingHand(false);
                event.setCanceled(true);
            }
        }
    }
}
