package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKConfig;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.client.gui.IPlayerDataAwareScreen;
import com.chaosbuffalo.mkcore.client.gui.ParticleEditorScreen;
import com.chaosbuffalo.mkcore.client.gui.PlayerPageRegistry;
import com.chaosbuffalo.mkcore.client.rendering.MKPlayerRenderer;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.attributes.MKRangedAttribute;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;
import com.chaosbuffalo.mkcore.init.CoreEffects;
import com.chaosbuffalo.mkcore.item.ArmorClass;
import com.chaosbuffalo.mkcore.item.AttributeTooltipManager;
import com.chaosbuffalo.mkcore.network.ExecuteActiveAbilityPacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.targeting_api.Targeting;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
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
import java.util.function.Consumer;


@EventBusSubscriber(modid = MKCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientEventHandler {

    private static final KeyMapping playerMenuBind = new KeyMapping("key.hud.playermenu",
            InputConstants.KEY_J, "key.mkcore.category");
    private static final KeyMapping particleEditorBind = new KeyMapping("key.hud.particle_editor",
            InputConstants.KEY_ADD, "key.mkcore.category");
    private static KeyMapping[] activeAbilityBinds;
    private static KeyMapping[] ultimateAbilityBinds;
    private static KeyMapping itemAbilityBind;

    @EventBusSubscriber(modid = MKCore.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
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

    // FIXME: Stun maybe needs to be done differently raw mouse event seems gone
//    @SubscribeEvent
//    public static void onRawMouseEvent(InputEvent.RawMouseEvent event) {
//        Minecraft minecraft = Minecraft.getInstance();
//        MKCore.getEntityData(minecraft.player).ifPresent(playerData -> {
//            if (playerData.getEffects().isEffectActive(CoreEffects.STUN.get()) &&
//                    minecraft.screen == null) {
//                event.setCanceled(true);
//            }
//        });
//    }

    @SubscribeEvent
    public static void cancelHealth(RenderGuiLayerEvent.Pre event) {
        if (event.getName().equals(VanillaGuiLayers.PLAYER_HEALTH)) {
            event.setCanceled(true);
        }
    }

    static void handleAbilityBarPressed(MKPlayerData player, AbilityGroupId group, int slot) {
        if (player.getAbilityExecutor().isOnGlobalCooldown() ||
                player.getEffects().isEffectActive(CoreEffects.STUN.get()))
            return;

        if (player.getAbilityExecutor().clientSimulateAbility(group, slot)) {
            MKCore.LOGGER.debug("sending execute ability {} {}", group, slot);
            PacketHandler.sendMessageToServer(new ExecuteActiveAbilityPacket(group, slot));
            player.getAbilityExecutor().startGlobalCooldown();
        }
    }

    public static void handleInputEvent() {
        Player player = Minecraft.getInstance().player;
        if (player == null)
            return;

        MKPlayerData playerData = MKCore.getPlayerOrNull(player);
        if (playerData == null)
            return;

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
    public static void doArmorClassTooltip(ItemTooltipEvent event) {
        // Don't do anything during the initial search tree population
        if (event.getEntity() == null)
            return;

        addArmorClassTooltip(event);
    }

    public static void setupAttributeRenderers() {
        AttributeTooltipManager.registerAttributeRenderer(MKAttributes.MAX_POISE, ClientEventHandler::renderPoise);
        AttributeTooltipManager.registerAttributeRenderer(MKAttributes.BLOCK_EFFICIENCY, ClientEventHandler::renderAbsolutePercentTwoDigits);
        AttributeTooltipManager.registerAttributeRenderer(MKAttributes.MELEE_CRIT, ClientEventHandler::renderAbsolutePercentTwoDigits);
        AttributeTooltipManager.registerAttributeRenderer(MKAttributes.MELEE_CRIT_MULTIPLIER, ClientEventHandler::renderCritMultiplier);
    }

    static void renderPoise(ItemStack stack, Player player,
                            Holder<Attribute> attribute, AttributeModifier modifier, Consumer<Component> output) {
        output.accept(AttributeTooltipManager.makePlusOrTakeText(attribute, modifier,
                modifier.amount(), modifier.amount()));
    }

    static void renderAbsolutePercentTwoDigits(ItemStack stack, Player player,
                                               Holder<Attribute> attribute, AttributeModifier modifier,
                                               Consumer<Component> output) {
        output.accept(AttributeTooltipManager.makeEqualsText(attribute, modifier,
                modifier.amount() * 100, v -> String.format("%.2f%%", v)));
    }

    static void renderCritMultiplier(ItemStack stack, Player player, Holder<Attribute> attribute,
                                     AttributeModifier modifier, Consumer<Component> output) {
        double value = player.getAttributeBaseValue(attribute) + modifier.amount();
        output.accept(AttributeTooltipManager.makeEqualsText(attribute, modifier, value,
                v -> String.format("%.1fx", v)));
    }

    private static void addArmorClassTooltip(ItemTooltipEvent event) {
        if (!MKConfig.CLIENT.showArmorClassOnTooltip.get())
            return;

        if (event.getItemStack().getItem() instanceof ArmorItem armorItem) {
            ArmorClass armorClass = ArmorClass.getItemArmorClass(event.getItemStack());
            if (armorClass == null) {
                return;
            }

            event.getToolTip().add(Component.translatable("mkcore.gui.item.armor_class.name")
                    .append(": ")
                    .append(armorClass.getName()));

            if (MKConfig.CLIENT.showArmorClassEffectsOnTooltip.get()) {
                List<Component> tooltip = event.getToolTip();
                if (Screen.hasShiftDown()) {
                    armorClass.getPositiveModifierMap(armorItem.getEquipmentSlot())
                            .forEach(((attribute, modifier) -> addAttributeToTooltip(tooltip, attribute, modifier, ChatFormatting.GREEN)));
                    armorClass.getNegativeModifierMap(armorItem.getEquipmentSlot())
                            .forEach(((attribute, modifier) -> addAttributeToTooltip(tooltip, attribute, modifier, ChatFormatting.RED)));
                } else {
                    tooltip.add(Component.translatable("mkcore.gui.item.armor_class.effect_prompt"));
                }
            }
        }
    }

    private static void addAttributeToTooltip(List<Component> tooltip, Holder<Attribute> attribute,
                                              AttributeModifier modifier, ChatFormatting color) {
        String suffix = "";
        double amount = modifier.amount();
        if (modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
            if (attribute instanceof MKRangedAttribute mkRangedAttribute) {
                if (mkRangedAttribute.displayAdditionAsPercentage()) {
                    suffix = "%";
                    amount *= 100;
                }
            }
        } else if (modifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
            amount *= 100;
            suffix = "%";
        } else if (modifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE) {
            amount *= 100;
            suffix = "% of base";
        }
        String prefix = amount > 0 ? "+" : "";

        Component component = Component.translatable("mkcore.gui.item.armor_class.effect.name")
                .withStyle(color)
                .append(String.format(": %s%.2f%s ", prefix, amount, suffix))
                .append(Component.translatable(attribute.value().getDescriptionId()));

        tooltip.add(component);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAttackReplacement(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isAttack() && event.getHand() == InteractionHand.MAIN_HAND) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player != null && mc.crosshairPickEntity != null) {
                if (Targeting.isValidFriendly(player, mc.crosshairPickEntity)) {
                    event.setCanceled(true);
                }
            }
        }
    }
}
