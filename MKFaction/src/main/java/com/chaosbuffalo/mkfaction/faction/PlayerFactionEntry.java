package com.chaosbuffalo.mkfaction.faction;

import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import com.chaosbuffalo.targeting_api.Targeting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;


public class PlayerFactionEntry implements IMKSerializable<CompoundTag> {

    private final MKFaction faction;
    private final Consumer<PlayerFactionEntry> dirtyNotifier;
    private int factionScore;
    private PlayerFactionStatus factionStatus;

    public PlayerFactionEntry(MKFaction faction, Consumer<PlayerFactionEntry> dirtyNotifier) {
        this.faction = faction;
        this.dirtyNotifier = dirtyNotifier;
        reset();
    }

    public MKFaction getFaction() {
        return faction;
    }

    public ResourceLocation getFactionName() {
        return faction.getId();
    }

    public int getFactionScore() {
        return factionScore;
    }

    public void setFactionScore(int factionScore) {
        this.factionScore = factionScore;
        factionStatus = PlayerFactionStatus.forScore(factionScore);
        markDirty();
    }

    public void reset() {
        setFactionScore(faction.getDefaultPlayerScore());
    }

    public void incrementFaction(int toAdd) {
        setFactionScore(factionScore + toAdd);
    }

    public void decrementFaction(int toSub) {
        setFactionScore(factionScore - toSub);
    }

    public Targeting.TargetRelation getTargetRelation() {
        return getFactionStatus().getRelation();
    }

    public PlayerFactionStatus getFactionStatus() {
        return factionStatus;
    }

    public MutableComponent getStatusDisplayName() {
        return faction.getStatusName(factionStatus);
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("factionScore", getFactionScore());
        return tag;
    }

    @Override
    public boolean deserialize(CompoundTag nbt) {
        if (nbt.contains("factionScore")) {
            setFactionScore(nbt.getInt("factionScore"));
        }
        return true;
    }

    private void markDirty() {
        if (dirtyNotifier != null) {
            dirtyNotifier.accept(this);
        }
    }
}
