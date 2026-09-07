package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public record MKWorkspacePlannerId(String value) {
    private static final Pattern SEGMENT_PATTERN = Pattern.compile("[a-z0-9_]+");

    public static final Codec<MKWorkspacePlannerId> CODEC = Codec.STRING.comapFlatMap(
            value -> {
                try {
                    return DataResult.success(MKWorkspacePlannerId.of(value));
                } catch (IllegalArgumentException ex) {
                    return DataResult.error(ex::getMessage);
                }
            },
            MKWorkspacePlannerId::value
    );

    public MKWorkspacePlannerId {
        value = normalize(value);
        validatePath(value);
    }

    public static MKWorkspacePlannerId of(String value) {
        return new MKWorkspacePlannerId(value);
    }

    public MKWorkspacePlannerId child(String segment) {
        String normalized = normalize(segment);
        validateSegment(normalized);
        return new MKWorkspacePlannerId(value + "." + normalized);
    }

    public Optional<MKWorkspacePlannerId> parent() {
        int index = value.lastIndexOf('.');
        if (index < 0) {
            return Optional.empty();
        }
        return Optional.of(new MKWorkspacePlannerId(value.substring(0, index)));
    }

    public boolean startsWith(MKWorkspacePlannerId parent) {
        return value.equals(parent.value()) || value.startsWith(parent.value() + ".");
    }

    public String lastSegment() {
        int index = value.lastIndexOf('.');
        return index < 0 ? value : value.substring(index + 1);
    }

    public List<String> segments() {
        return List.copyOf(Arrays.asList(value.split("\\.")));
    }

    public int segmentCount() {
        int count = 1;
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == '.') {
                count++;
            }
        }
        return count;
    }

    public String toExportName() {
        return value.replace('.', '_');
    }

    @Override
    public String toString() {
        return value;
    }

    private static String normalize(String value) {
        if (value == null) {
            throw new IllegalArgumentException("planner id cannot be null");
        }
        return value.trim();
    }

    private static void validatePath(String value) {
        if (value.isEmpty()) {
            throw new IllegalArgumentException("planner id cannot be empty");
        }
        if (value.startsWith(".") || value.endsWith(".")) {
            throw new IllegalArgumentException("planner id cannot start or end with '.'");
        }
        if (value.contains("..")) {
            throw new IllegalArgumentException("planner id cannot contain empty segments");
        }
        for (String segment : value.split("\\.")) {
            validateSegment(segment);
        }
    }

    private static void validateSegment(String segment) {
        if (segment.isEmpty()) {
            throw new IllegalArgumentException("planner id segment cannot be empty");
        }
        if (!SEGMENT_PATTERN.matcher(segment).matches()) {
            throw new IllegalArgumentException("planner id segment must match [a-z0-9_]+: " + segment);
        }
    }
}
