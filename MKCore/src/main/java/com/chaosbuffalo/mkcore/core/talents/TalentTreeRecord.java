package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.sync.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TalentTreeRecord {
    private final TalentTreeDefinition tree;
    private final Map<String, TalentLineRecord> lines = new HashMap<>();
    private final TalentTreeUpdater updater;

    public TalentTreeRecord(TalentTreeDefinition tree) {
        this.tree = tree;
        updater = new TalentTreeUpdater();
    }

    @Nonnull
    ISyncObject getUpdater() {
        return updater;
    }

    @Nonnull
    public TalentTreeDefinition getTreeDefinition() {
        return tree;
    }

    public Stream<TalentRecord> getRecordStream() {
        return lines.values().stream()
                .flatMap(e -> e.getRecords().stream());
    }

    public int getPointsSpent() {
        return getRecordStream()
                .filter(TalentRecord::isKnown)
                .mapToInt(TalentRecord::getRank)
                .sum();
    }

    @Nullable
    public TalentRecord getNodeRecord(String lineName, int index) {
        TalentLineRecord lineRecord = getLineRecord(lineName);
        if (lineRecord == null)
            return null;
        return lineRecord.getRecord(index);
    }

    @Nullable
    private TalentLineRecord getLineRecord(String lineName) {
        return lines.computeIfAbsent(lineName, this::createLineRecord);
    }

    private boolean validatePointModification(TalentRecord record, int amount) {
        TalentNode node = record.getNode();
        String lineName = node.getLine().getName();
        int index = node.getIndex();

        TalentLineRecord lineRecord = getLineRecord(lineName);
        if (lineRecord == null) {
            MKCore.LOGGER.error("validatePointModification({}, {}, {}) - line does not exist", lineName, index, amount);
            return false;
        }

        if (index >= lineRecord.getLength()) {
            MKCore.LOGGER.error("validatePointModification({}, {}, {}) - index out of range (max {})", lineName, index, amount, lineRecord.getLength());
            return false;
        }

        if (amount > 0) {
            // trying to add
            if (index != 0) {
                TalentRecord previous = lineRecord.getRecord(index - 1);
                if (!previous.isKnown()) {
                    MKCore.LOGGER.error("validatePointModification({}, {}, {}) - cannot learn talent if the previous is unknown", lineName, index, amount);
                    return false;
                }
            }

            return record.getRank() < node.getMaxRanks();
        } else if (amount < 0) {
            // trying to remove
            TalentRecord next = lineRecord.getRecord(index + 1);
            if (next != null && next.isKnown() && record.getRank() <= 1) {
                MKCore.LOGGER.error("validatePointModification({}, {}, {}) - cannot unlearn talent if children have points", lineName, index, amount);
                return false;
            }

            return record.getRank() > 0;
        }

        return false;
    }

    private boolean modifyPoint(TalentRecord record, int points) {
        if (!validatePointModification(record, points))
            return false;

        if (record.modifyRank(points)) {
            TalentNode node = record.getNode();
            updater.markUpdated(node.getLine().getName(), node.getIndex());
            return true;
        }
        return false;
    }

    public boolean trySpendPoint(String line, int index) {
        MKCore.LOGGER.debug("trySpendPoint({}, {})", line, index);
        TalentRecord record = getNodeRecord(line, index);
        if (record == null)
            return false;

        int amount = 1;
        return modifyPoint(record, amount);
    }

    public boolean tryRefundPoint(String line, int index) {
        MKCore.LOGGER.debug("tryRefundPoint({}, {})", line, index);
        TalentRecord record = getNodeRecord(line, index);
        if (record == null || !record.isKnown())
            return false;

        int amount = -1;
        return modifyPoint(record, amount);
    }

    public <T> T serialize(DynamicOps<T> ops) {
        ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
        builder.put(ops.createString("version"), ops.createInt(tree.getVersion()));
        builder.put(
                ops.createString("lines"),
                ops.createMap(
                        lines.entrySet()
                                .stream()
                                .collect(Collectors.toMap(
                                        k -> ops.createString(k.getKey()),
                                        v -> v.getValue().serialize(ops)))
                )
        );
        return ops.createMap(builder.build());
    }

    public <T> boolean deserialize(Dynamic<T> dynamic) {

        int version = dynamic.get("version").asInt(-1);
        if (version != tree.getVersion()) {
            // This isn't really an error if it's an upgrade scenario.
            // Return true to add it to the unlocked tree map but with no points spent
            return true;
        }

        Map<DataResult<String>, Dynamic<T>> lineMap = dynamic.get("lines").asMap(Dynamic::asString, Function.identity());
        lineMap.forEach((name, value) ->
                name.resultOrPartial(MKCore.LOGGER::error).ifPresent(s -> deserializeLineRecord(s, value)));

        return true;
    }

    private <T> void deserializeLineRecord(String name, Dynamic<T> dyn) {
        TalentLineRecord lineRecord = createLineRecord(name);
        if (lineRecord == null) {
            MKCore.LOGGER.error("TalentTreeRecord.deserializeLineRecord line {} - line does not exist!", name);
            return;
        }

        if (lineRecord.deserialize(dyn)) {
            lines.put(name, lineRecord);
        } else {
            MKCore.LOGGER.error("TalentTreeRecord.deserializeLineRecord line {} - line failed to deserialize!", name);
        }
    }

    private TalentLineRecord createLineRecord(String name) {
        TalentLineDefinition lineDef = tree.getLine(name);
        if (lineDef != null) {
            return new TalentLineRecord(lineDef);
        }
        return null;
    }

    private static class TalentLineRecord {
        private final TalentLineDefinition lineDefinition;
        private final List<TalentRecord> lineRecords;

        public TalentLineRecord(TalentLineDefinition lineDefinition) {
            this.lineDefinition = lineDefinition;
            this.lineRecords = lineDefinition.getNodes()
                    .stream()
                    .map(TalentNode::createRecord)
                    .collect(Collectors.toList());
        }

        public TalentRecord getRecord(int index) {
            if (index < lineRecords.size()) {
                return lineRecords.get(index);
            }
            return null;
        }

        public List<TalentRecord> getRecords() {
            return Collections.unmodifiableList(lineRecords);
        }

        public int getLength() {
            return lineDefinition.getLength();
        }

        public TalentLineDefinition getLineDefinition() {
            return lineDefinition;
        }


        public <T> T serialize(DynamicOps<T> ops) {
            return ops.createList(lineRecords.stream().map(record -> record.serialize(ops)));
        }

        public <T> boolean deserialize(Dynamic<T> dynamic) {
            List<Dynamic<T>> entries = dynamic.asList(Function.identity());
            int savedCount = entries.size();
            if (savedCount != getLength())
                return false;

            for (int i = 0; i < savedCount; i++) {
                TalentRecord record = getRecord(i);
                if (!record.deserialize(entries.get(i))) {
                    return false;
                }
            }
            return true;
        }
    }

    private class TalentTreeUpdater implements ISyncObject {
        private final HashMap<String, BitSet> updatedLines = new HashMap<>();
        private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

        public void markUpdated(String lineName, int index) {
            getLineUpdater(lineName).set(index);
            parentNotifier.notifyUpdate(this);
        }

        private BitSet getLineUpdater(String line) {
            return updatedLines.computeIfAbsent(line, k -> new BitSet());
        }

        @Override
        public void setNotifier(ISyncNotifier notifier) {
            parentNotifier = notifier;
        }

        @Override
        public boolean isDirty() {
            return !updatedLines.isEmpty();
        }

        @Override
        public void deserializeUpdate(CompoundTag tag) {
            CompoundTag root = tag.getCompound(getTreeDefinition().getTreeId().toString());

            if (root.getBoolean("f")) {
                lines.clear();
            }

            if (root.contains("u")) {
                CompoundTag updated = root.getCompound("u");

                for (String line : updated.getAllKeys()) {
                    TalentLineRecord lineRecord = getLineRecord(line);
                    if (lineRecord == null) {
                        MKCore.LOGGER.warn("TalentTreeUpdater.deserializeUpdate unknown line {}", line);
                        continue;
                    }
                    updated.getList(line, Tag.TAG_COMPOUND).forEach(nbt -> {
                        int index = ((CompoundTag) nbt).getInt("i");
                        TalentRecord record = lineRecord.getRecord(index);
                        if (record != null) {
                            record.deserialize(new Dynamic<>(NbtOps.INSTANCE, nbt));
                        }
                    });
                }
            }
        }

        private CompoundTag writeNode(TalentRecord rec) {
            CompoundTag recTag = (CompoundTag) rec.serialize(NbtOps.INSTANCE);
            recTag.putInt("i", rec.getNode().getIndex());
            return recTag;
        }

        @Override
        public void serializeUpdate(CompoundTag tag) {
            CompoundTag root = new CompoundTag();

            CompoundTag updateTag = new CompoundTag();
            updatedLines.forEach((key, bits) -> {
                TalentLineRecord lineRecord = getLineRecord(key);
                if (lineRecord == null) {
                    return;
                }

                ListTag list = bits.stream()
                        .mapToObj(lineRecord::getRecord)
                        .map(this::writeNode)
                        .collect(Collectors.toCollection(ListTag::new));
                updateTag.put(key, list);
            });

            root.put("u", updateTag);
            tag.put(getTreeDefinition().getTreeId().toString(), root);

            updatedLines.clear();
        }

        @Override
        public void serializeFull(CompoundTag tag) {
            CompoundTag root = new CompoundTag();
            root.putBoolean("f", true);

            CompoundTag updateTag = new CompoundTag();

            lines.values().forEach(line -> {
                String lineName = line.getLineDefinition().getName();
                ListTag list = line.getRecords().stream()
                        .map(this::writeNode)
                        .collect(Collectors.toCollection(ListTag::new));
                updateTag.put(lineName, list);
            });

            root.put("u", updateTag);
            tag.put(getTreeDefinition().getTreeId().toString(), root);

            updatedLines.clear();
        }
    }
}
