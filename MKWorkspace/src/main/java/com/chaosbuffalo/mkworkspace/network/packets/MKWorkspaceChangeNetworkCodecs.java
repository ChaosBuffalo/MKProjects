package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeEffect;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeSummary;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceFieldChange;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceGeneratedLayer;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceMutationSafety;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

final class MKWorkspaceChangeNetworkCodecs {
    private MKWorkspaceChangeNetworkCodecs() {
    }

    static void writeSummaryMetadata(FriendlyByteBuf buffer, MKWorkspaceChangeSummary summary) {
        buffer.writeResourceLocation(summary.operationId());
        buffer.writeUtf(summary.title());
        buffer.writeUtf(summary.summary());
        buffer.writeEnum(summary.safety());
        buffer.writeBoolean(summary.backupRequired());
        writeEnumList(buffer, summary.invalidatedLayers());
        writeStrings(buffer, summary.warnings());
        writeStrings(buffer, summary.blockers());
        buffer.writeVarInt(summary.fieldChanges().size());
        for (MKWorkspaceFieldChange fieldChange : summary.fieldChanges()) {
            buffer.writeUtf(fieldChange.field());
            buffer.writeUtf(fieldChange.beforeValue());
            buffer.writeUtf(fieldChange.afterValue());
        }
    }

    static MKWorkspaceChangeSummary readSummaryMetadata(FriendlyByteBuf buffer) {
        var operationId = buffer.readResourceLocation();
        String title = buffer.readUtf();
        String summary = buffer.readUtf();
        MKWorkspaceMutationSafety safety = buffer.readEnum(MKWorkspaceMutationSafety.class);
        boolean backupRequired = buffer.readBoolean();
        List<MKWorkspaceGeneratedLayer> invalidated = readEnumList(buffer, MKWorkspaceGeneratedLayer.class);
        List<String> warnings = readStrings(buffer);
        List<String> blockers = readStrings(buffer);
        int fieldCount = buffer.readVarInt();
        ArrayList<MKWorkspaceFieldChange> fields = new ArrayList<>(fieldCount);
        for (int i = 0; i < fieldCount; i++) {
            fields.add(new MKWorkspaceFieldChange(buffer.readUtf(), buffer.readUtf(), buffer.readUtf()));
        }
        return new MKWorkspaceChangeSummary(operationId, title, summary, safety, backupRequired, invalidated,
                warnings, blockers, fields, List.of());
    }

    static void writeEffects(FriendlyByteBuf buffer, List<MKWorkspaceChangeEffect> effects) {
        buffer.writeVarInt(effects.size());
        for (MKWorkspaceChangeEffect effect : effects) {
            buffer.writeEnum(effect.action());
            buffer.writeEnum(effect.subject());
            buffer.writeUtf(effect.subjectId());
            buffer.writeUtf(effect.displayName());
            buffer.writeUtf(effect.baseName());
            buffer.writeVarInt(effect.variantIndex());
            buffer.writeBoolean(effect.physical());
            buffer.writeBoolean(effect.derived());
            buffer.writeUtf(effect.detail());
        }
    }

    static List<MKWorkspaceChangeEffect> readEffects(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        ArrayList<MKWorkspaceChangeEffect> effects = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            effects.add(new MKWorkspaceChangeEffect(
                    buffer.readEnum(MKWorkspaceChangeEffect.Action.class),
                    buffer.readEnum(MKWorkspaceChangeEffect.Subject.class),
                    buffer.readUtf(), buffer.readUtf(), buffer.readUtf(), buffer.readVarInt(),
                    buffer.readBoolean(), buffer.readBoolean(), buffer.readUtf()));
        }
        return List.copyOf(effects);
    }

    private static void writeStrings(FriendlyByteBuf buffer, List<String> values) {
        buffer.writeVarInt(values.size());
        values.forEach(buffer::writeUtf);
    }

    private static List<String> readStrings(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        ArrayList<String> values = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            values.add(buffer.readUtf());
        }
        return List.copyOf(values);
    }

    private static <E extends Enum<E>> void writeEnumList(FriendlyByteBuf buffer, List<E> values) {
        buffer.writeVarInt(values.size());
        values.forEach(buffer::writeEnum);
    }

    private static <E extends Enum<E>> List<E> readEnumList(FriendlyByteBuf buffer, Class<E> enumClass) {
        int count = buffer.readVarInt();
        ArrayList<E> values = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            values.add(buffer.readEnum(enumClass));
        }
        return List.copyOf(values);
    }
}
