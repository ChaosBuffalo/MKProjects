package com.chaosbuffalo.mkfaction.faction;

import com.chaosbuffalo.mkcore.serialization.IDynamicMapSerializer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.network.chat.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class FactionGreetings implements IDynamicMapSerializer {

    private final Set<Component> battlecries;
    private final Set<Component> outsiderGreetings;
    private final Set<Component> friendlyGreetings;
    private final Set<Component> memberGreetings;

    private final Set<Component> EMPTY = ImmutableSet.of();

    public enum GreetingType {
        OUTSIDER,
        FRIENDLY,
        MEMBER,
        BATTLECRY
    }

    private final MKFaction faction;

    public FactionGreetings(MKFaction faction) {
        battlecries = new HashSet<>();
        outsiderGreetings = new HashSet<>();
        friendlyGreetings = new HashSet<>();
        memberGreetings = new HashSet<>();
        this.faction = faction;
    }

    protected Set<Component> getGreetingsForType(GreetingType type) {
        switch (type) {
            case MEMBER -> {
                return memberGreetings;
            }
            case OUTSIDER -> {
                return outsiderGreetings;
            }
            case FRIENDLY -> {
                return friendlyGreetings;
            }
            case BATTLECRY -> {
                return battlecries;
            }
        }
        return EMPTY;
    }

    public Optional<Set<Component>> getGreetingsWithMembers(GreetingType type) {
        Set<Component> greetings = getGreetingsForType(type);
        return greetings.size() > 0 ? Optional.of(greetings) : Optional.empty();
    }

    public FactionGreetings addGreeting(GreetingType type, Component greeting) {
        getGreetingsForType(type).add(greeting);
        return this;
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        battlecries.clear();
        deserializeComponentList(dynamic, "battlecries", battlecries::add);
        outsiderGreetings.clear();
        deserializeComponentList(dynamic, "outsider_greetings", outsiderGreetings::add);
        friendlyGreetings.clear();
        deserializeComponentList(dynamic, "friendly_greetings", friendlyGreetings::add);
        memberGreetings.clear();
        deserializeComponentList(dynamic, "member_greetings", memberGreetings::add);
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        builder.put(ops.createString("battlecries"), ops.createList(battlecries.stream()
                .map(x -> ops.createString(Component.Serializer.toJson(x)))));
        builder.put(ops.createString("outsider_greetings"), ops.createList(outsiderGreetings.stream()
                .map(x -> ops.createString(Component.Serializer.toJson(x)))));
        builder.put(ops.createString("friendly_greetings"), ops.createList(friendlyGreetings.stream()
                .map(x -> ops.createString(Component.Serializer.toJson(x)))));
        builder.put(ops.createString("member_greetings"), ops.createList(memberGreetings.stream()
                .map(x -> ops.createString(Component.Serializer.toJson(x)))));
    }

    private <D> void deserializeComponentList(Dynamic<D> dynamic, String listName, Consumer<Component> consumer) {
        dynamic.get(listName).asStream()
                .map(x -> x.asString().map(Component.Serializer::fromJson).result()
                        .orElseThrow(() -> new IllegalStateException("Failed to parse entry in '" + listName +
                                "' for faction greetings '" + faction.getId() + "': " + x)))
                .forEach(consumer);
    }
}
