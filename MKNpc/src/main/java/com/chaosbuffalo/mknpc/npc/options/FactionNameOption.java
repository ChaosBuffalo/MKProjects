package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mkfaction.event.MKFactionRegistry;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.content.ContentDB;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.option_entries.FactionNameOptionEntry;
import com.chaosbuffalo.mknpc.npc.option_entries.INameEntry;
import com.chaosbuffalo.mknpc.npc.option_entries.INpcOptionEntry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.*;

public class FactionNameOption extends WorldPermanentOption implements INameProvider {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "faction_name");
    public static final Codec<FactionNameOption> CODEC = RecordCodecBuilder.<FactionNameOption>mapCodec(builder -> {
        return builder.group(
                Codec.STRING.optionalFieldOf("title").forGetter(i -> Optional.ofNullable(i.title)),
                Codec.BOOL.optionalFieldOf("hasLastName", false).forGetter(i -> i.hasLastName)
        ).apply(builder, FactionNameOption::new);
    }).codec();

    @Nullable
    private String title;
    private boolean hasLastName;

    private FactionNameOption(Optional<String> title, boolean hasLastName) {
        super(NAME, ApplyOrder.LATE);
        this.title = title.orElse(null);
        this.hasLastName = hasLastName;
    }

    public FactionNameOption() {
        super(NAME, ApplyOrder.LATE);
        hasLastName = false;
    }

    public FactionNameOption setTitle(String title) {
        this.title = title;
        return this;
    }

    public FactionNameOption setHasLastName(boolean value) {
        this.hasLastName = value;
        return this;
    }


    @Override
    @Nullable
    public MutableComponent getEntityName(NpcDefinition definition, Level world, UUID spawnId) {
        return ContentDB.tryGetLevelData(world).map(cap -> {
            if (!cap.hasEntityOptionEntry(definition, this, spawnId)) {
                cap.addEntityOptionEntry(definition, this, spawnId, makeOptionEntry(definition, world.getRandom()));
            }
            INpcOptionEntry entry = cap.getEntityOptionEntry(definition, this, spawnId);
            if (entry instanceof INameEntry nameEntry) {
                return nameEntry.getName();
            } else {
                return Component.literal("Name Error");
            }
        }).orElseGet(() -> Component.literal("Name Error"));
    }

    @Nullable
    @Override
    public String getDisplayName() {
        String name = "";
        if (title != null) {
            name += title;
        }
        name += " ";
        name += "[First]";
        if (hasLastName) {
            name += " ";
            name += "[Last]";
        }
        return name;
    }

    @Nullable
    private static <T> T getRandomEntry(RandomSource random, Set<T> set) {
        if (set.isEmpty()) {
            return null;
        }
        List<T> list = new ArrayList<>(set);
        return list.get(random.nextInt(list.size()));
    }

    @Override
    protected INpcOptionEntry makeOptionEntry(NpcDefinition definition, RandomSource random) {
        String name = "";
        if (title != null) {
            name += title;
        }
        MKFaction faction = MKFactionRegistry.getFaction(definition.getFactionName());
        if (faction != null) {
            name += " ";
            String firstName = getRandomEntry(random, faction.getFirstNames());
            if (firstName == null) {
                firstName = "No Name";
            }
            name += firstName;
            if (hasLastName) {
                name += " ";
                String lastName = getRandomEntry(random, faction.getLastNames());
                if (lastName == null) {
                    lastName = "Unknown";
                }
                name += lastName;
            }
        }
        return new FactionNameOptionEntry(name);
    }
}
