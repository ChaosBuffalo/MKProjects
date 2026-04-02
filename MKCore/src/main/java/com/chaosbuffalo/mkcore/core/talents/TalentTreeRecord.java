package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.sync.SyncContext;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import com.chaosbuffalo.mkcore.sync.v2.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.v2.ISyncObject;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceKey;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TalentTreeRecord {
    private final TalentTreeDefinition tree;
    private final ResourceKey<TalentTreeDefinition> treeId;
    private final Map<String, TalentLineRecord> lines = new HashMap<>();
    private final TalentTreeUpdater updater;

    public TalentTreeRecord(TalentTreeDefinition tree, ResourceKey<TalentTreeDefinition> treeId) {
        this.tree = tree;
        this.treeId = treeId;
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

    public ResourceKey<TalentTreeDefinition> getTreeId() {
        return treeId;
    }

    public Stream<TalentRecord> getRecordStream() {
        return lines.values().stream()
                .flatMap(e -> e.lineRecords.stream());
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
                if (previous == null || !previous.isKnown()) {
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
        TalentRecord record = getNodeRecord(line, index);
        if (record == null)
            return false;

        int amount = 1;
        return modifyPoint(record, amount);
    }

    public boolean tryRefundPoint(String line, int index) {
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
            // Not necessarily an error, but just install a blank tree record and let the player put points in again
            return false;
        }

        Map<DataResult<String>, Dynamic<T>> lineMap = dynamic.get("lines").asMap(Dynamic::asString, Function.identity());
        for (Map.Entry<DataResult<String>, Dynamic<T>> entry : lineMap.entrySet()) {
            String name = entry.getKey().getOrThrow();
            if (!deserializeLineRecord(name, entry.getValue())) {
                return false;
            }
        }

        return true;
    }

    private <T> boolean deserializeLineRecord(String name, Dynamic<T> dyn) {
        TalentLineRecord lineRecord = createLineRecord(name);
        if (lineRecord == null) {
            MKCore.LOGGER.error("TalentTreeRecord.deserializeLineRecord line {} - line does not exist!", name);
            return false;
        }

        if (lineRecord.deserialize(dyn)) {
            lines.put(name, lineRecord);
        } else {
            MKCore.LOGGER.error("TalentTreeRecord.deserializeLineRecord line {} - line failed to deserialize!", name);
            return false;
        }
        return true;
    }

    private TalentLineRecord createLineRecord(String name) {
        TalentLineDefinition lineDef = tree.getLine(name);
        if (lineDef != null) {
            return new TalentLineRecord(lineDef, this);
        }
        return null;
    }

    private static class TalentLineRecord {
        private final List<TalentRecord> lineRecords;

        public TalentLineRecord(TalentLineDefinition lineDefinition, TalentTreeRecord treeRecord) {
            this.lineRecords = lineDefinition.getNodes()
                    .stream()
                    .map(node -> new TalentRecord(node, treeRecord))
                    .collect(Collectors.toList());
        }

        public TalentRecord getRecord(int index) {
            if (index < lineRecords.size()) {
                return lineRecords.get(index);
            }
            return null;
        }

        public int getLength() {
            return lineRecords.size();
        }


        public <T> T serialize(DynamicOps<T> ops) {
            return ops.createList(lineRecords.stream()
                    .takeWhile(TalentRecord::isKnown)
                    .map(record -> record.serialize(ops)));
        }

        public <T> boolean deserialize(Dynamic<T> dynamic) {
            List<Dynamic<T>> storedEntries = dynamic.asList(Function.identity());
            if (storedEntries.size() > getLength())
                return false;

            for (int i = 0; i < lineRecords.size(); i++) {
                TalentRecord record = lineRecords.get(i);
                if (record == null) {
                    return false;
                }
                if (i < storedEntries.size()) {
                    if (!record.deserialize(storedEntries.get(i))) {
                        return false;
                    }
                } else {
                    // Probably not needed, but explicitly set the remaining nodes to 0
                    record.setRank(0);
                }
            }

            // Reject edited/corrupt saves that skip prerequisite nodes, e.g. node 3 known while node 2 is 0.
            boolean foundGap = false;
            for (TalentRecord record : lineRecords) {
                if (!record.isKnown()) {
                    foundGap = true;
                } else if (foundGap) {
                    return false;
                }
            }
            return true;
        }
    }

    private class TalentTreeUpdater implements ISyncObject {
        private final Map<String, BitSet> updatedLines = new HashMap<>();
        private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

        public void markUpdated(String lineName, int index) {
            getLineUpdater(lineName).set(index);
            parentNotifier.notifyUpdate();
        }

        private BitSet getLineUpdater(String line) {
            return updatedLines.computeIfAbsent(line, k -> new BitSet());
        }

        @Override
        public void setSyncUpdateNotifier(ISyncNotifier notifier) {
            parentNotifier = notifier;
        }

        @Override
        public boolean isDirty() {
            return !updatedLines.isEmpty();
        }

        @Override
        public void clearDirty() {
            updatedLines.clear();
        }

        private IntArrayTag compressRecords(Stream<TalentRecord> recordStream) {
            int[] nodeInfo = recordStream
                    .mapMultiToInt((record, mapper) -> {
                        mapper.accept(record.getNode().getIndex());
                        mapper.accept(record.getRank());
                    })
                    .toArray();
            return new IntArrayTag(nodeInfo);
        }

        @Override
        public @Nullable Tag writeFullValue(SyncContext context, SyncVisibility visibility) {
            CompoundTag updateTag = new CompoundTag();

            lines.forEach((name, line) -> {
                var knownRecords = line.lineRecords.stream().takeWhile(TalentRecord::isKnown);
                updateTag.put(name, compressRecords(knownRecords));
            });

            return updateTag;
        }

        @Override
        public @Nullable Tag writeDirtyValue(SyncContext context, SyncVisibility visibility) {
            CompoundTag updateTag = new CompoundTag();
            updatedLines.forEach((key, bits) -> {
                TalentLineRecord lineRecord = getLineRecord(key);
                if (lineRecord == null) {
                    return;
                }
                var dirtyRecords = bits.stream().mapToObj(lineRecord::getRecord);
                updateTag.put(key, compressRecords(dirtyRecords));
            });

            updatedLines.clear();
            return updateTag;
        }

        @Override
        public void handleUpdatePayload(SyncContext context, Tag valueTag, SyncVisibility visibility) {
            if (valueTag instanceof CompoundTag updated) {
                for (String line : updated.getAllKeys()) {
                    TalentLineRecord lineRecord = getLineRecord(line);
                    if (lineRecord == null) {
                        MKCore.LOGGER.warn("TalentTreeUpdater received unknown line {}", line);
                        continue;
                    }

                    int[] nodeInfo = updated.getIntArray(line);
                    if (nodeInfo.length % 2 != 0) {
                        MKCore.LOGGER.warn("TalentTreeUpdater improper node info length {}", nodeInfo.length);
                        continue;
                    }
                    for (int i = 0; i < nodeInfo.length; i += 2) {
                        int index = nodeInfo[i];
                        int rank = nodeInfo[i + 1];
                        TalentRecord record = lineRecord.getRecord(index);
                        if (record != null) {
                            record.setRank(rank);
                        }
                    }
                }
            }
        }
    }
}
