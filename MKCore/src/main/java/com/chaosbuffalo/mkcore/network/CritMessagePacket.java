package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKConfig;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CritMessagePacket {
    public enum CritType {
        MELEE_CRIT,
        MK_CRIT,
        PROJECTILE_CRIT,
        TYPED_CRIT
    }

    private final int targetId;
    private ResourceLocation abilityName;
    private ResourceLocation damageType;
    private final float critDamage;
    private final CritType type;
    private int projectileId;
    private String typeName;
    private final int sourceId;


    public CritMessagePacket(int targetId, int sourceId, float critDamage) {
        this.targetId = targetId;
        this.sourceId = sourceId;
        this.critDamage = critDamage;
        this.type = CritType.MELEE_CRIT;
    }

    public CritMessagePacket(int targetId, int sourceId, float critDamage, MKDamageType damageType, String typeName) {
        this.targetId = targetId;
        this.sourceId = sourceId;
        this.critDamage = critDamage;
        this.type = CritType.TYPED_CRIT;
        this.typeName = typeName;
        this.damageType = damageType.getId();
    }


    public CritMessagePacket(int targetId, int sourceId, float critDamage, ResourceLocation abilityName,
                             MKDamageType damageType) {
        this.targetId = targetId;
        this.sourceId = sourceId;
        this.critDamage = critDamage;
        this.type = CritType.MK_CRIT;
        this.abilityName = abilityName;
        this.damageType = damageType.getId();
    }

    public CritMessagePacket(int targetId, int sourceId, float critDamage, int projectileId) {
        this.type = CritType.PROJECTILE_CRIT;
        this.targetId = targetId;
        this.sourceId = sourceId;
        this.critDamage = critDamage;
        this.projectileId = projectileId;
    }

    public CritMessagePacket(FriendlyByteBuf pb) {
        this.type = pb.readEnum(CritType.class);
        this.targetId = pb.readInt();
        sourceId = pb.readInt();
        this.critDamage = pb.readFloat();
        if (type == CritType.MK_CRIT) {
            this.abilityName = pb.readResourceLocation();
            this.damageType = pb.readResourceLocation();
        }
        if (type == CritType.PROJECTILE_CRIT) {
            this.projectileId = pb.readInt();
        }
        if (type == CritType.TYPED_CRIT) {
            this.damageType = pb.readResourceLocation();
            this.typeName = pb.readUtf();
        }
    }

    public void toBytes(FriendlyByteBuf pb) {
        pb.writeEnum(type);
        pb.writeInt(targetId);
        pb.writeInt(sourceId);
        pb.writeFloat(critDamage);
        if (type == CritType.MK_CRIT) {
            pb.writeResourceLocation(this.abilityName);
            pb.writeResourceLocation(this.damageType);
        }
        if (type == CritType.PROJECTILE_CRIT) {
            pb.writeInt(this.projectileId);
        }
        if (type == CritType.TYPED_CRIT) {
            pb.writeResourceLocation(damageType);
            pb.writeUtf(typeName);
        }
    }

    public static void handle(CritMessagePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> ClientHandler.handleClient(packet));
        ctx.setPacketHandled(true);
    }

    static class ClientHandler {
        public static void handleClient(CritMessagePacket packet) {
            Player player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }
            Entity source = player.getCommandSenderWorld().getEntity(packet.sourceId);
            Entity target = player.getCommandSenderWorld().getEntity(packet.targetId);
            if (target == null || source == null) {
                return;
            }
            boolean isSelf = player.is(source);
            boolean isSelfTarget = player.getId() == packet.targetId;
            if (isSelf || isSelfTarget) {
                if (!MKConfig.CLIENT.showMyCrits.get()) {
                    return;
                }
            } else {
                if (!MKConfig.CLIENT.showOthersCrits.get()) {
                    return;
                }
            }
            if (!(source instanceof LivingEntity)) {
                return;
            }
            LivingEntity livingSource = (LivingEntity) source;
            switch (packet.type) {
                case MELEE_CRIT:
                    if (isSelf) {
                        player.sendMessage(new TranslatableComponent("mkcore.crit.melee.self",
                                target.getDisplayName(),
                                livingSource.getMainHandItem().getHoverName(),
                                Math.round(packet.critDamage)
                        ).withStyle(ChatFormatting.DARK_RED), Util.NIL_UUID);
                    } else {
                        player.sendMessage(new TranslatableComponent("mkcore.crit.melee.other",
                                livingSource.getDisplayName(),
                                target.getDisplayName(),
                                livingSource.getMainHandItem().getHoverName(),
                                Math.round(packet.critDamage)
                        ).withStyle(ChatFormatting.DARK_RED), Util.NIL_UUID);
                    }
                    break;
                case MK_CRIT:
//                messageStyle.setColor(TextFormatting.AQUA);
                    MKAbility ability = MKCoreRegistry.getAbility(packet.abilityName);
                    MKDamageType mkDamageType = MKCoreRegistry.getDamageType(packet.damageType);
                    if (ability == null || mkDamageType == null) {
                        break;
                    }
                    player.sendMessage(mkDamageType.getAbilityCritMessage(livingSource, (LivingEntity) target, packet.critDamage, ability, isSelf), Util.NIL_UUID);
                    break;
                case PROJECTILE_CRIT:
                    Entity projectile = player.getCommandSenderWorld().getEntity(packet.projectileId);
                    if (projectile != null) {
                        if (isSelf) {
                            player.sendMessage(new TranslatableComponent("mkcore.crit.projectile.self",
                                    target.getDisplayName(),
                                    projectile.getDisplayName(),
                                    Math.round(packet.critDamage)
                            ).withStyle(ChatFormatting.LIGHT_PURPLE), Util.NIL_UUID);
                        } else {
                            player.sendMessage(new TranslatableComponent("mkcore.crit.projectile.other",
                                    livingSource.getDisplayName(),
                                    target.getDisplayName(),
                                    projectile.getDisplayName(),
                                    Math.round(packet.critDamage)
                            ).withStyle(ChatFormatting.LIGHT_PURPLE), Util.NIL_UUID);
                        }
                    }
                    break;
                case TYPED_CRIT:
                    mkDamageType = MKCoreRegistry.getDamageType(packet.damageType);
                    if (mkDamageType == null) {
                        break;
                    }
                    player.sendMessage(mkDamageType.getEffectCritMessage(livingSource, (LivingEntity) target, packet.critDamage, packet.typeName, isSelf), Util.NIL_UUID);
                    break;
            }
        }
    }
}