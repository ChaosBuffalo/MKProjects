package com.chaosbuffalo.mkfaction.faction;

import com.chaosbuffalo.mkfaction.MKFactionMod;
import com.chaosbuffalo.mkfaction.capabilities.FactionCapabilities;
import com.chaosbuffalo.mkfaction.event.MKFactionRegistry;
import com.chaosbuffalo.targeting_api.Targeting;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class MKFaction {
    public static final ResourceLocation INVALID_FACTION = new ResourceLocation(MKFactionMod.MODID, "faction.invalid");
    private final Set<ResourceLocation> allies;
    private final Set<ResourceLocation> enemies;
    private final Set<String> firstNames;
    private final Set<String> lastNames;
    private final EnumMap<PlayerFactionStatus, String> customStatusNames = new EnumMap<>(PlayerFactionStatus.class);
    private int defaultPlayerScore;

    private final FactionGreetings greetings;

    public MKFaction(int defaultPlayerScore) {
        this(defaultPlayerScore, new HashSet<>(), new HashSet<>());
    }

    public MKFaction(int defaultPlayerScore, Set<ResourceLocation> allies,
                     Set<ResourceLocation> enemies) {
        this.allies = allies;
        this.enemies = enemies;
        this.defaultPlayerScore = defaultPlayerScore;
        this.firstNames = new HashSet<>();
        this.lastNames = new HashSet<>();
        greetings = new FactionGreetings(this);
    }

    public ResourceLocation getId() {
        return MKFactionRegistry.FACTION_REGISTRY.getKey(this);
    }

    public String getTranslationKey() {
        ResourceLocation factionId = getId();
        if (factionId != null) {
            return factionId.toLanguageKey("faction", "name");
        } else {
            return "faction.mkfaction.invalid.name";
        }
    }

    public FactionGreetings getGreetings() {
        return greetings;
    }

    public MutableComponent getDisplayName() {
        return Component.translatable(getTranslationKey());
    }

    public MutableComponent getStatusName(PlayerFactionStatus status) {
        String customName = customStatusNames.get(status);
        if (customName != null) {
            return Component.literal(customName);
        }
        return status.getDefaultDisplayName();
    }

    public Set<String> getFirstNames() {
        return firstNames;
    }

    public Set<String> getLastNames() {
        return lastNames;
    }

    public void addFirstName(String name) {
        firstNames.add(name);
    }

    public void addLastName(String name) {
        lastNames.add(name);
    }

    public int getDefaultPlayerScore() {
        return defaultPlayerScore;
    }

    public void setDefaultPlayerScore(int defaultPlayerScore) {
        this.defaultPlayerScore = defaultPlayerScore;
    }

    public Set<ResourceLocation> getAllies() {
        return allies;
    }

    public Set<ResourceLocation> getEnemies() {
        return enemies;
    }

    public MKFaction addAlly(ResourceLocation allyName) {
        allies.add(allyName);
        return this;
    }

    public MKFaction addEnemy(ResourceLocation enemyName) {
        enemies.add(enemyName);
        return this;
    }

    public void setStatusName(PlayerFactionStatus status, String name) {
        customStatusNames.put(status, name);
    }

    public boolean isEnemy(ResourceLocation faction) {
        return enemies.contains(faction);
    }

    public boolean isAlly(ResourceLocation faction) {
        return allies.contains(faction);
    }

    public boolean isMember(LivingEntity entity) {
        if (entity instanceof Player) {
            return false;
        } else {
            return entity.getCapability(FactionCapabilities.MOB_FACTION_CAPABILITY)
                    .map(cap -> cap.getFactionName().equals(getId()))
                    .orElse(false);
        }
    }

    public <D> D serialize(DynamicOps<D> ops) {
        ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
        builder.put(ops.createString("defaultPlayerScore"), ops.createInt(defaultPlayerScore));
        builder.put(ops.createString("allies"), ops.createList(allies.stream()
                .map(ResourceLocation::toString).sorted().map(ops::createString)));
        builder.put(ops.createString("enemies"), ops.createList(enemies.stream()
                .map(ResourceLocation::toString).sorted().map(ops::createString)));
        builder.put(ops.createString("firstNames"), ops.createList(firstNames.stream().sorted().map(ops::createString)));
        builder.put(ops.createString("lastNames"), ops.createList(lastNames.stream().sorted().map(ops::createString)));
        if (!customStatusNames.isEmpty()) {
            ImmutableMap.Builder<D, D> nameBuilder = ImmutableMap.builder();
            customStatusNames.forEach((status, name) -> nameBuilder.put(ops.createString(status.name()), ops.createString(name)));
            builder.put(ops.createString("statusNames"), ops.createMap(nameBuilder.build()));
        }
        builder.put(ops.createString("greetings"), greetings.serialize(ops));
        return ops.createMap(builder.build());
    }

    public <D> void deserialize(Dynamic<D> dynamic) {
        defaultPlayerScore = dynamic.get("defaultPlayerScore").asInt(FactionConstants.TRUE_NEUTRAL);

        allies.clear();
        deserializeFactionList(dynamic, "allies", allies::add);

        enemies.clear();
        deserializeFactionList(dynamic, "enemies", enemies::add);

        firstNames.clear();
        deserializeNameList(dynamic, "firstNames", firstNames::add);

        lastNames.clear();
        deserializeNameList(dynamic, "lastNames", lastNames::add);

        dynamic.get("greetings").get().result().ifPresent(greetings::deserialize);

        customStatusNames.clear();
        dynamic.get("statusNames").asMap(this::getStringOrThrow, this::getStringOrThrow).forEach((keyString, value) -> {
            PlayerFactionStatus status = PlayerFactionStatus.valueOf(keyString);
            customStatusNames.put(status, value);
        });
    }

    private <D> String getStringOrThrow(Dynamic<D> dynamic) {
        return dynamic.asString().getOrThrow(false, MKFactionMod.LOGGER::error);
    }

    private <D> void deserializeNameList(Dynamic<D> dynamic, String listName, Consumer<String> consumer) {
        dynamic.get(listName).asStream()
                .map(x -> x.asString().result()
                        .orElseThrow(() -> new IllegalStateException("Failed to parse entry in '" + listName +
                                "' for faction '" + getId() + "': " + x)))
                .forEach(consumer);
    }

    private <D> void deserializeFactionList(Dynamic<D> dynamic, String listName, Consumer<ResourceLocation> consumer) {
        dynamic.get(listName).asStream()
                .map(x -> x.asString().result()
                        .map(ResourceLocation::new)
                        .orElseThrow(() -> new IllegalStateException("Failed to parse entry in '" + listName +
                                "' for faction '" + getId() + "': " + x)))
                .forEach(consumer);
    }

    public Targeting.TargetRelation getNonPlayerEntityRelationship(LivingEntity entity, ResourceLocation factionName,
                                                                   MKFaction otherFaction) {
        if (isMember(entity)) {
            return Targeting.TargetRelation.FRIEND;
        } else if (isEnemy(factionName)) {
            return Targeting.TargetRelation.ENEMY;
        } else if (isAlly(factionName)) {
            return Targeting.TargetRelation.FRIEND;
        } else {
            if (otherFaction == null) {
                return Targeting.TargetRelation.NEUTRAL;
            }
            PlayerFactionStatus thisPlayerFaction = PlayerFactionStatus.forScore(getDefaultPlayerScore());
            PlayerFactionStatus otherPlayerFaction = PlayerFactionStatus.forScore(otherFaction.getDefaultPlayerScore());
            if (thisPlayerFaction.isOpposite(otherPlayerFaction)) {
                return Targeting.TargetRelation.ENEMY;
            }
            return Targeting.TargetRelation.NEUTRAL;
        }
    }
}
