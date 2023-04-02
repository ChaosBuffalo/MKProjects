package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mkfaction.event.MKFactionRegistry;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.NpcCapabilities;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.option_entries.FactionNameOptionEntry;
import com.chaosbuffalo.mknpc.npc.option_entries.INameEntry;
import com.chaosbuffalo.mknpc.npc.option_entries.INpcOptionEntry;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.*;

public class FactionNameOption extends WorldPermanentOption implements INameProvider {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "faction_name");

    @Nullable
    private String title;
    private boolean hasLastName;

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
    public TextComponent getEntityName(NpcDefinition definition, Level world, UUID spawnId) {
        return world.getCapability(NpcCapabilities.WORLD_NPC_DATA_CAPABILITY).map(
                cap -> {
                    if (!cap.hasEntityOptionEntry(definition, this, spawnId)) {
                        cap.addEntityOptionEntry(definition, this, spawnId, makeOptionEntry(definition, world.getRandom()));
                    }
                    INpcOptionEntry entry = cap.getEntityOptionEntry(definition, this, spawnId);
                    if (entry instanceof INameEntry) {
                        return ((INameEntry) entry).getName();
                    } else {
                        return new TextComponent("Name Error");
                    }
                }).orElse(new TextComponent("Name Error"));
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
    private static <T> T getRandomEntry(Random random, Set<T> set) {
        List<T> list = new ArrayList<>(set);
        if (list.size() <= 0) {
            return null;
        }
        return list.get(random.nextInt(list.size()));
    }

    @Override
    protected INpcOptionEntry makeOptionEntry(NpcDefinition definition, Random random) {
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


    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        this.title = dynamic.get("title").asString(null);
        this.hasLastName = dynamic.get("hasLastName").asBoolean(false);
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("title"), ops.createString(title));
        builder.put(ops.createString("hasLastName"), ops.createBoolean(hasLastName));
    }
}
