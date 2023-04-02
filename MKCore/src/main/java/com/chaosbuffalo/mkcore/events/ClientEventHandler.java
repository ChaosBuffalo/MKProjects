package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKConfig;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.client.gui.IPlayerDataAwareScreen;
import com.chaosbuffalo.mkcore.client.gui.ParticleEditorScreen;
import com.chaosbuffalo.mkcore.client.gui.PlayerPageRegistry;
import com.chaosbuffalo.mkcore.core.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.MKRangedAttribute;
import com.chaosbuffalo.mkcore.init.CoreEffects;
import com.chaosbuffalo.mkcore.item.ArmorClass;
import com.chaosbuffalo.mkcore.item.AttributeTooltipManager;
import com.chaosbuffalo.mkcore.network.ExecuteActiveAbilityPacket;
import com.chaosbuffalo.mkcore.network.MKItemAttackPacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.targeting_api.Targeting;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, value = Dist.CLIENT)
public class ClientEventHandler {

    private static KeyMapping playerMenuBind;
    private static KeyMapping particleEditorBind;
    private static KeyMapping[] activeAbilityBinds;
    private static KeyMapping[] ultimateAbilityBinds;
    private static KeyMapping itemAbilityBind;

    public static void initKeybindings() {
        playerMenuBind = new KeyMapping("key.hud.playermenu", GLFW.GLFW_KEY_J, "key.mkcore.category");
        ClientRegistry.registerKeyBinding(playerMenuBind);

        particleEditorBind = new KeyMapping("key.hud.particle_editor", GLFW.GLFW_KEY_KP_ADD, "key.mkcore.category");
        ClientRegistry.registerKeyBinding(particleEditorBind);

        activeAbilityBinds = new KeyMapping[GameConstants.MAX_BASIC_ABILITIES];
        for (int i = 0; i < GameConstants.MAX_BASIC_ABILITIES; i++) {
            String bindName = String.format("key.hud.active_ability%d", i + 1);
            int key = GLFW.GLFW_KEY_1 + i;
            KeyMapping bind = new KeyMapping(bindName, KeyConflictContext.IN_GAME, KeyModifier.ALT,
                    InputConstants.getKey(key, 0), "key.mkcore.abilitybar");

            ClientRegistry.registerKeyBinding(bind);
            activeAbilityBinds[i] = bind;
        }

        ultimateAbilityBinds = new KeyMapping[GameConstants.MAX_ULTIMATE_ABILITIES];
        for (int i = 0; i < GameConstants.MAX_ULTIMATE_ABILITIES; i++) {
            String bindName = String.format("key.hud.ultimate_ability%d", i + 1);
            int key = GLFW.GLFW_KEY_6 + i;
            KeyMapping bind = new KeyMapping(bindName, KeyConflictContext.IN_GAME, KeyModifier.ALT,
                    InputConstants.getKey(key, 0), "key.mkcore.abilitybar");

            ClientRegistry.registerKeyBinding(bind);
            ultimateAbilityBinds[i] = bind;
        }


        int defaultItemKey = GLFW.GLFW_KEY_8;
        itemAbilityBind = new KeyMapping("key.hud.item_ability", KeyConflictContext.IN_GAME, KeyModifier.ALT,
                InputConstants.getKey(defaultItemKey, 0), "key.mkcore.abilitybar");
        ClientRegistry.registerKeyBinding(itemAbilityBind);
    }

    public static float getTotalGlobalCooldown() {
        return (float) GameConstants.GLOBAL_COOLDOWN_TICKS / GameConstants.TICKS_PER_SECOND;
    }

    @SubscribeEvent
    public static void onKeyEvent(InputEvent.KeyInputEvent event) {
        handleInputEvent();
    }

    @SubscribeEvent
    public static void onMouseEvent(InputEvent.MouseInputEvent event) {
        handleInputEvent();
    }

    @SubscribeEvent
    public static void onRawMouseEvent(InputEvent.RawMouseEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        MKCore.getEntityData(minecraft.player).ifPresent(playerData -> {
            if (playerData.getEffects().isEffectActive(CoreEffects.STUN.get()) &&
                    minecraft.screen == null) {
                event.setCanceled(true);
            }
        });
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
    public static void onRender(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Minecraft inst = Minecraft.getInstance();
            Player player = inst.player;
            if (player != null) {
                double dist = player.getAttribute(MKAttributes.ATTACK_REACH).getValue();
                float partialTicks = inst.getFrameTime();
                HitResult result = player.pick(dist, partialTicks, false);
                Vec3 eyePos = player.getEyePosition(partialTicks);

                double tracedDist2 = dist * dist;
                if (result != null) {
                    tracedDist2 = result.getLocation().distanceToSqr(eyePos);
                }
                Vec3 lookVec = player.getViewVector(1.0f);
                Vec3 to = eyePos.add(lookVec.x * dist, lookVec.y * dist, lookVec.z * dist);
                AABB lookBB = player.getBoundingBox().expandTowards(lookVec.scale(dist)).inflate(1.0D, 1.0D, 1.0D);
                EntityHitResult entityTrace = ProjectileUtil.getEntityHitResult(player, eyePos, to, lookBB,
                        (ent) -> !ent.isSpectator() && ent.isPickable(), tracedDist2);
                MKCore.getPlayer(player).ifPresent(x -> {
                    if (entityTrace != null) {
                        Entity entityHit = entityTrace.getEntity();
                        x.getCombatExtension().setPointedEntity(entityHit);
                    } else {
                        x.getCombatExtension().setPointedEntity(null);
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDataUpdated(PlayerDataEvent.Updated event) {
        if (event.getPlayer().getCommandSenderWorld().isClientSide) {
            Player local = Minecraft.getInstance().player;
            if (local == null || !event.getPlayer().is(local))
                return;

            if (Minecraft.getInstance().screen instanceof IPlayerDataAwareScreen) {
                ((IPlayerDataAwareScreen) Minecraft.getInstance().screen).onPlayerDataUpdate();
            }
        }
    }

    @SubscribeEvent
    public static void doArmorClassTooltip(ItemTooltipEvent event) {
        // Don't do anything during the initial search tree population
        if (event.getPlayer() == null)
            return;

        addArmorClassTooltip(event);
    }

    public static void setupAttributeRenderers() {
        AttributeTooltipManager.registerAttributeRenderer(MKAttributes.MAX_POISE, ClientEventHandler::renderPoise);
        AttributeTooltipManager.registerAttributeRenderer(MKAttributes.BLOCK_EFFICIENCY, ClientEventHandler::renderAbsolutePercentTwoDigits);
        AttributeTooltipManager.registerAttributeRenderer(MKAttributes.MELEE_CRIT, ClientEventHandler::renderAbsolutePercentTwoDigits);
        AttributeTooltipManager.registerAttributeRenderer(MKAttributes.MELEE_CRIT_MULTIPLIER, ClientEventHandler::renderCritMultiplier);
    }

    static List<Component> renderPoise(ItemStack stack, EquipmentSlot slotType, Player player,
                                       Attribute attribute, AttributeModifier modifier) {
        return Collections.singletonList(
                AttributeTooltipManager.makeBonusOrTakeText(attribute, modifier,
                        modifier.getAmount(), modifier.getAmount()));
    }

    static List<Component> renderAbsolutePercentTwoDigits(ItemStack stack, EquipmentSlot slotType,
                                                          Player player, Attribute attribute,
                                                          AttributeModifier modifier) {
        return Collections.singletonList(
                AttributeTooltipManager.makeAbsoluteText(attribute, modifier,
                        modifier.getAmount() * 100, v -> String.format("%.2f%%", v)));
    }

    static List<Component> renderCritMultiplier(ItemStack stack, EquipmentSlot slotType,
                                                Player player, Attribute attribute,
                                                AttributeModifier modifier) {
        double value = player.getAttributeBaseValue(attribute) + modifier.getAmount();
        return Collections.singletonList(
                AttributeTooltipManager.makeAbsoluteText(attribute, modifier,
                        value, v -> String.format("%.1fx", v)));
    }

    private static void addArmorClassTooltip(ItemTooltipEvent event) {
        if (!MKConfig.CLIENT.showArmorClassOnTooltip.get())
            return;

        if (event.getItemStack().getItem() instanceof ArmorItem) {
            ArmorItem armorItem = (ArmorItem) event.getItemStack().getItem();
            ArmorClass armorClass = ArmorClass.getItemArmorClass(armorItem);
            if (armorClass == null) {
                return;
            }

            event.getToolTip().add(new TranslatableComponent("mkcore.gui.item.armor_class.name")
                    .append(": ")
                    .append(armorClass.getName()));

            if (MKConfig.CLIENT.showArmorClassEffectsOnTooltip.get()) {
                List<Component> tooltip = event.getToolTip();
                if (Screen.hasShiftDown()) {
                    armorClass.getPositiveModifierMap(armorItem.getSlot())
                            .forEach(((attribute, modifier) -> addAttributeToTooltip(tooltip, attribute, modifier, ChatFormatting.GREEN)));
                    armorClass.getNegativeModifierMap(armorItem.getSlot())
                            .forEach(((attribute, modifier) -> addAttributeToTooltip(tooltip, attribute, modifier, ChatFormatting.RED)));
                } else {
                    tooltip.add(new TranslatableComponent("mkcore.gui.item.armor_class.effect_prompt"));
                }
            }
        }
    }

    private static void addAttributeToTooltip(List<Component> tooltip, Attribute attribute,
                                              AttributeModifier modifier, ChatFormatting color) {
        String suffix = "";
        double amount = modifier.getAmount();
        if (modifier.getOperation() == AttributeModifier.Operation.ADDITION) {
            if (attribute instanceof MKRangedAttribute) {
                if (((MKRangedAttribute) attribute).displayAdditionAsPercentage()) {
                    suffix = "%";
                    amount *= 100;
                }
            }
        }
        if (modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL) {
            amount *= 100;
            suffix = "%";
        } else if (modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE) {
            amount *= 100;
            suffix = "% of base";
        }
        String prefix = amount > 0 ? "+" : "";

        Component component = new TranslatableComponent("mkcore.gui.item.armor_class.effect.name")
                .withStyle(color)
                .append(String.format(": %s%.2f%s ", prefix, amount, suffix))
                .append(new TranslatableComponent(attribute.getDescriptionId()));

        tooltip.add(component);
    }

    private static void doPlayerAttack(Player player, Entity target, Minecraft minecraft) {
        if (minecraft.gameMode != null) {
            minecraft.gameMode.ensureHasSentCarriedItem();
        }
        PacketHandler.sendMessageToServer(new MKItemAttackPacket(target));
        if (!player.isSpectator()) {
            player.attack(target);
            player.resetAttackStrengthTicker();
            MKCore.getEntityData(player).ifPresent(cap -> cap.getCombatExtension().recordSwing());
            MinecraftForge.EVENT_BUS.post(new PostAttackEvent(player));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAttackReplacement(InputEvent.ClickInputEvent event) {
        if (event.isAttack() && event.getHand() == InteractionHand.MAIN_HAND) {
            Minecraft inst = Minecraft.getInstance();
            Player player = inst.player;
            if (player != null) {
                Optional<Entity> lookingAt = MKCore.getPlayer(player)
                        .map(x -> x.getCombatExtension().getPointedEntity())
                        .orElse(Optional.empty());
                lookingAt.ifPresent(entityHit -> {
                    if (!Targeting.isValidFriendly(player, entityHit)) {
                        doPlayerAttack(player, entityHit, Minecraft.getInstance());
                        event.setSwingHand(true);
                    }
                    event.setCanceled(true);
                });
            }
        }
    }
}
