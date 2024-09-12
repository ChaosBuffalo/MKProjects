package com.chaosbuffalo.mkfaction.faction;

import com.chaosbuffalo.mkcore.serialization.IDynamicMapSerializer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class FactionGreetings implements IDynamicMapSerializer {
    public static final MapCodec<FactionGreetings> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ComponentSerialization.CODEC.listOf().fieldOf("battlecries").forGetter(i -> i.battlecries),
            ComponentSerialization.CODEC.listOf().fieldOf("outsider_greetings").forGetter(i -> i.outsiderGreetings),
            ComponentSerialization.CODEC.listOf().fieldOf("friendly_greetings").forGetter(i -> i.friendlyGreetings),
            ComponentSerialization.CODEC.listOf().fieldOf("member_greetings").forGetter(i -> i.memberGreetings)
    ).apply(builder, FactionGreetings::new));

    private final List<Component> battlecries;
    private final List<Component> outsiderGreetings;
    private final List<Component> friendlyGreetings;
    private final List<Component> memberGreetings;

    private final List<Component> EMPTY = ImmutableList.of();

    public enum GreetingType {
        OUTSIDER,
        FRIENDLY,
        MEMBER,
        BATTLECRY
    }

    private FactionGreetings(List<Component> battlecries, List<Component> outsiderGreetings,
                             List<Component> friendlyGreetings, List<Component> memberGreetings) {
        this.battlecries = battlecries;
        this.outsiderGreetings = outsiderGreetings;
        this.friendlyGreetings = friendlyGreetings;
        this.memberGreetings = memberGreetings;
    }

    public FactionGreetings() {
        battlecries = new ArrayList<>();
        outsiderGreetings = new ArrayList<>();
        friendlyGreetings = new ArrayList<>();
        memberGreetings = new ArrayList<>();
    }

    protected List<Component> getGreetingsForType(GreetingType type) {
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

    public Optional<List<Component>> getGreetingsWithMembers(GreetingType type) {
        List<Component> greetings = getGreetingsForType(type);
        return !greetings.isEmpty() ? Optional.of(greetings) : Optional.empty();
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
                .map(x -> ComponentSerialization.CODEC.encodeStart(ops, x).getOrThrow())));
        builder.put(ops.createString("outsider_greetings"), ops.createList(outsiderGreetings.stream()
                .map(x -> ComponentSerialization.CODEC.encodeStart(ops, x).getOrThrow())));
        builder.put(ops.createString("friendly_greetings"), ops.createList(friendlyGreetings.stream()
                .map(x -> ComponentSerialization.CODEC.encodeStart(ops, x).getOrThrow())));
        builder.put(ops.createString("member_greetings"), ops.createList(memberGreetings.stream()
                .map(x -> ComponentSerialization.CODEC.encodeStart(ops, x).getOrThrow())));
    }

    private <D> void deserializeComponentList(Dynamic<D> dynamic, String listName, Consumer<Component> consumer) {
        dynamic.get(listName).asStream()
                .map(x -> ComponentSerialization.CODEC.parse(x).result()
                        .orElseThrow(() -> new IllegalStateException("Failed to parse entry in '" + listName +
                                "' for faction greetings: " + x)))
                .forEach(consumer);
    }
}
