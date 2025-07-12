package com.chaosbuffalo.mkfaction.faction;

import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import com.chaosbuffalo.targeting_api.Targeting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;


public class PlayerFactionEntry implements IMKSerializable<CompoundTag> {

    private final Holder<MKFaction> faction;
    private final Consumer<PlayerFactionEntry> dirtyNotifier;
    private int factionScore;
    private PlayerFactionStatus factionStatus;

    public PlayerFactionEntry(Holder<MKFaction> faction, Consumer<PlayerFactionEntry> dirtyNotifier) {
        this.faction = faction;
        this.dirtyNotifier = dirtyNotifier;
        reset();
    }

    public Holder<MKFaction> getFaction() {
        return faction;
    }

    public int getFactionScore() {
        return factionScore;
    }

    public void setFactionScore(int factionScore) {
        this.factionScore = factionScore;
        factionStatus = PlayerFactionStatus.forScore(factionScore);
        markDirty();
    }

    public MutableComponent getDisplayName() {
        return faction.value().getDisplayName();
    }

    public void reset() {
        setFactionScore(faction.value().getDefaultPlayerScore());
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
        return faction.value().getStatusName(factionStatus);
    }

    @Override
    public CompoundTag serialize(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("factionScore", getFactionScore());
        return tag;
    }

    @Override
    public boolean deserialize(HolderLookup.Provider provider, CompoundTag nbt) {
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
