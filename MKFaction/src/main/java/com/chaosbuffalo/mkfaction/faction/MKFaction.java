package com.chaosbuffalo.mkfaction.faction;

import com.chaosbuffalo.mkcore.utils.CommonCodecs;
import com.chaosbuffalo.mkfaction.MKFactionMod;
import com.chaosbuffalo.mkfaction.event.MKFactionRegistry;
import com.chaosbuffalo.targeting_api.Targeting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class MKFaction {
    public static final Codec<MKFaction> DIRECT_CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.<MKFaction>mapCodec(builder -> builder.group(
            Codec.INT.fieldOf("defaultPlayerScore").forGetter(i -> i.defaultPlayerScore),
            RegistryCodecs.homogeneousList(MKFactionRegistry.FACTION_REGISTRY_KEY, true).fieldOf("allies").forGetter(i -> i.allySet),
            RegistryCodecs.homogeneousList(MKFactionRegistry.FACTION_REGISTRY_KEY, true).fieldOf("enemies").forGetter(i -> i.enemySet),
            CommonCodecs.sortedSet(Codec.STRING, String::compareToIgnoreCase).fieldOf("firstNames").forGetter(i -> i.firstNames),
            CommonCodecs.sortedSet(Codec.STRING, String::compareToIgnoreCase).fieldOf("lastNames").forGetter(i -> i.lastNames),
            FactionGreetings.CODEC.fieldOf("messages").forGetter(i -> i.greetings)
    ).apply(builder, MKFaction::new)).codec());

    public static final Codec<Holder<MKFaction>> CODEC = RegistryFileCodec.create(MKFactionRegistry.FACTION_REGISTRY_KEY, DIRECT_CODEC);
    public static final Codec<Holder<MKFaction>> REFERENCE_CODEC = RegistryFixedCodec.create(MKFactionRegistry.FACTION_REGISTRY_KEY);


    public static final ResourceLocation INVALID_FACTION = MKFactionMod.id("faction.invalid");
    private final HolderSet<MKFaction> allySet;
    private final HolderSet<MKFaction> enemySet;
    private final Set<String> firstNames;
    private final Set<String> lastNames;
    private final int defaultPlayerScore;
    private final FactionGreetings greetings;

    private MKFaction(int defaultPlayerScore, HolderSet<MKFaction> allySet,
                      HolderSet<MKFaction> enemySet,
                      Set<String> firstNames, Set<String> lastNames, FactionGreetings greetings) {
        this.defaultPlayerScore = defaultPlayerScore;
        this.allySet = allySet;
        this.enemySet = enemySet;
        this.firstNames = firstNames;
        this.lastNames = lastNames;
        this.greetings = greetings;
    }

    public MKFaction(int defaultPlayerScore) {
        this.defaultPlayerScore = defaultPlayerScore;
        allySet = HolderSet.empty();
        enemySet = HolderSet.empty();
        this.firstNames = new HashSet<>();
        this.lastNames = new HashSet<>();
        greetings = new FactionGreetings();
    }


    public FactionGreetings getGreetings() {
        return greetings;
    }

    public static MutableComponent getDisplayName(ResourceLocation factionId) {
        return Component.translatable(factionId.toLanguageKey("faction", "name"));
    }

    public static MutableComponent getDisplayName(ResourceKey<MKFaction> factionId) {
        return getDisplayName(factionId.location());
    }

    public MutableComponent getStatusName(PlayerFactionStatus status) {
        return status.getDefaultDisplayName();
    }

    public Set<String> getFirstNames() {
        return firstNames;
    }

    public Set<String> getLastNames() {
        return lastNames;
    }

    public int getDefaultPlayerScore() {
        return defaultPlayerScore;
    }

    public boolean isEnemy(Holder<MKFaction> faction) {
        return enemySet.contains(faction);
    }

    public boolean isAlly(Holder<MKFaction> faction) {
        return allySet.contains(faction);
    }

    public Targeting.TargetRelation getNonPlayerEntityRelationship(LivingEntity opponent, @Nullable Holder<MKFaction> opponentFaction) {
        if (opponentFaction == null) {
            // Opponent did not have a faction, cannot make a determination
            return Targeting.TargetRelation.UNHANDLED;
        }

        if (opponentFaction.value() == this) {
            return Targeting.TargetRelation.FRIEND;
        } else if (isEnemy(opponentFaction)) {
            return Targeting.TargetRelation.ENEMY;
        } else if (isAlly(opponentFaction)) {
            return Targeting.TargetRelation.FRIEND;
        } else {
            PlayerFactionStatus thisPlayerFaction = PlayerFactionStatus.forScore(getDefaultPlayerScore());
            PlayerFactionStatus otherPlayerFaction = PlayerFactionStatus.forScore(opponentFaction.value().getDefaultPlayerScore());
            if (thisPlayerFaction.isOpposite(otherPlayerFaction)) {
                return Targeting.TargetRelation.ENEMY;
            }
            return Targeting.TargetRelation.NEUTRAL;
        }
    }

    public static class Builder {
        private static int keyCompare(ResourceKey<MKFaction> a, ResourceKey<MKFaction> b) {
            int ret = a.registry().compareTo(b.registry());
            if (ret == 0) {
                ret = a.location().compareNamespaced(b.location());
            }
            return ret;
        }

        private final Set<ResourceKey<MKFaction>> allies = new TreeSet<>(Builder::keyCompare);
        private final Set<ResourceKey<MKFaction>> enemies = new TreeSet<>(Builder::keyCompare);
        private final Set<String> firstNames = new TreeSet<>(String::compareToIgnoreCase);
        private final Set<String> lastNames = new TreeSet<>(String::compareToIgnoreCase);
        private final int defaultPlayerScore;

        private final FactionGreetings greetings = new FactionGreetings();

        public Builder(int defaultPlayerScore) {
            this.defaultPlayerScore = defaultPlayerScore;
        }

        public Builder addAlly(ResourceKey<MKFaction> ally) {
            allies.add(ally);
            return this;
        }

        public Builder addAlly(Collection<ResourceKey<MKFaction>> ally) {
            allies.addAll(ally);
            return this;
        }

        public Builder addEnemy(ResourceKey<MKFaction> ally) {
            enemies.add(ally);
            return this;
        }

        public Builder addEnemy(Collection<ResourceKey<MKFaction>> ally) {
            enemies.addAll(ally);
            return this;
        }

        public Builder addFirstName(String name) {
            firstNames.add(name);
            return this;
        }

        public Builder addLastName(String name) {
            lastNames.add(name);
            return this;
        }

        public FactionGreetings getGreetings() {
            return greetings;
        }

        private HolderSet<MKFaction> holderSet(Set<ResourceKey<MKFaction>> set, BootstrapContext<MKFaction> context) {
            var lookup = context.lookup(MKFactionRegistry.FACTION_REGISTRY_KEY);
            return HolderSet.direct(lookup::getOrThrow, set);
        }

        public MKFaction build(BootstrapContext<MKFaction> context) {
            return new MKFaction(defaultPlayerScore,
                    holderSet(allies, context),
                    holderSet(enemies, context),
                    firstNames,
                    lastNames,
                    greetings);
        }


    }
}
