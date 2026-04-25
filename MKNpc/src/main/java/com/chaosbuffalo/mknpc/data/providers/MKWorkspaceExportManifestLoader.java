package com.chaosbuffalo.mknpc.data.providers;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.world.gen.workspace.export.MKWorkspaceExportManifest;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MKWorkspaceExportManifestLoader {
    private static final List<Path> RELATIVE_ROOTS = List.of(
            Paths.get("generated"),
            Paths.get("workspace_exports"),
            Paths.get("src", "main", "resources", "data"),
            Paths.get("src", "generated", "resources", "data"),
            Paths.get("build", "resources", "main", "data"),
            Paths.get("MKNpc", "src", "main", "resources", "data"),
            Paths.get("MKNpc", "src", "generated", "resources", "data"),
            Paths.get("MKNpc", "build", "resources", "main", "data")
    );

    public record LoadedManifest(Path path, MKWorkspaceExportManifest manifest) {
    }

    public static List<LoadedManifest> loadAll() {
        Map<String, LoadedManifest> manifests = new LinkedHashMap<>();
        for (Path root : candidateRoots()) {
            if (!Files.exists(root)) {
                continue;
            }
            scanRoot(root, manifests);
        }
        if (manifests.isEmpty()) {
            scanRoot(Paths.get("."), 8, manifests);
        }
        return new ArrayList<>(manifests.values());
    }

    private static List<Path> candidateRoots() {
        LinkedHashSet<Path> roots = new LinkedHashSet<>();
        Path cwd = Paths.get("").toAbsolutePath().normalize();
        for (Path base = cwd; base != null; base = base.getParent()) {
            for (Path relativeRoot : RELATIVE_ROOTS) {
                roots.add(base.resolve(relativeRoot).normalize());
            }
        }
        return new ArrayList<>(roots);
    }

    private static void scanRoot(Path root, Map<String, LoadedManifest> manifests) {
        scanRoot(root, Integer.MAX_VALUE, manifests);
    }

    private static void scanRoot(Path root, int maxDepth, Map<String, LoadedManifest> manifests) {
        try (Stream<Path> stream = Files.walk(root, maxDepth)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .filter(path -> path.toString().replace('\\', '/').contains("/mk_workspace_exports/"))
                    .forEach(path -> loadOne(path).ifPresent(loaded -> manifests.put(manifestKey(loaded.manifest()), loaded)));
        } catch (IOException e) {
            MKNpc.LOGGER.warn("Failed to scan workspace export manifests under {}", root, e);
        }
    }

    private static java.util.Optional<LoadedManifest> loadOne(Path path) {
        try (Reader reader = Files.newBufferedReader(path)) {
            var json = JsonParser.parseReader(reader);
            MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
            return java.util.Optional.of(new LoadedManifest(path, manifest));
        } catch (Exception e) {
            MKNpc.LOGGER.warn("Failed to load workspace export manifest {}", path, e);
            return java.util.Optional.empty();
        }
    }

    private static String manifestKey(MKWorkspaceExportManifest manifest) {
        return manifest.namespace() + ":" + manifest.structureName();
    }
}
