package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.export;

import com.chaosbuffalo.mkworkspaceruntime.MKWorkspaceRuntime;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class MKWorkspaceExportManifestLoader {
    public record LoadedManifest(Path path, MKWorkspaceExportManifest manifest) {
    }

    public static Path resolveModuleRoot(String moduleDirectoryName) {
        Path cwd = Paths.get("").toAbsolutePath().normalize();
        for (Path candidate : candidateModuleRoots(cwd, moduleDirectoryName)) {
            if (isModuleRoot(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to resolve module root for " + moduleDirectoryName + " from " + cwd);
    }

    public static List<LoadedManifest> loadAllFromModSource(Path moduleRoot, String namespace) {
        Path manifestDir = manifestDirectory(moduleRoot, namespace);
        if (!Files.isDirectory(manifestDir)) {
            return List.of();
        }

        try (Stream<Path> stream = Files.list(manifestDir)) {
            return stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .map(path -> loadOne(path, namespace))
                    .flatMap(java.util.Optional::stream)
                    .sorted((left, right) -> left.manifest().structureName().compareToIgnoreCase(right.manifest().structureName()))
                    .toList();
        } catch (IOException e) {
            MKWorkspaceRuntime.LOGGER.warn("Failed to scan workspace export manifests under {}", manifestDir, e);
            return List.of();
        }
    }

    public static java.util.Optional<LoadedManifest> loadFromModSource(Path moduleRoot, ResourceLocation id) {
        Path manifestPath = manifestDirectory(moduleRoot, id.getNamespace()).resolve(id.getPath() + ".json");
        if (!Files.isRegularFile(manifestPath)) {
            return java.util.Optional.empty();
        }
        return loadOne(manifestPath, id.getNamespace());
    }

    private static List<Path> candidateModuleRoots(Path cwd, String moduleDirectoryName) {
        java.util.ArrayList<Path> candidates = new java.util.ArrayList<>();
        addCandidate(candidates, cwd);
        addCandidate(candidates, cwd.resolve(moduleDirectoryName));
        for (Path parent = cwd.getParent(); parent != null; parent = parent.getParent()) {
            addCandidate(candidates, parent.resolve(moduleDirectoryName));
        }
        return candidates;
    }

    private static void addCandidate(List<Path> candidates, Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        if (!candidates.contains(normalized)) {
            candidates.add(normalized);
        }
    }

    private static boolean isModuleRoot(Path path) {
        return Files.isDirectory(path.resolve(Paths.get("src", "main", "resources")));
    }

    private static Path manifestDirectory(Path moduleRoot, String namespace) {
        return moduleRoot.resolve(Paths.get("src", "main", "resources", "data", namespace, "mk_workspace_exports"));
    }

    private static java.util.Optional<LoadedManifest> loadOne(Path path, String expectedNamespace) {
        try (Reader reader = Files.newBufferedReader(path)) {
            var json = JsonParser.parseReader(reader);
            MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.CODEC.parse(JsonOps.INSTANCE, json)
                    .getOrThrow()
                    .withNormalizedRuntimeHints();
            if (!manifest.namespace().equals(expectedNamespace)) {
                MKWorkspaceRuntime.LOGGER.warn("Skipping workspace export manifest {} because namespace {} did not match expected {}",
                        path, manifest.namespace(), expectedNamespace);
                return java.util.Optional.empty();
            }
            if (!path.getFileName().toString().equals(manifest.structureName() + ".json")) {
                MKWorkspaceRuntime.LOGGER.warn("Skipping workspace export manifest {} because file name did not match structure name {}",
                        path, manifest.structureName());
                return java.util.Optional.empty();
            }
            return java.util.Optional.of(new LoadedManifest(path, manifest));
        } catch (Exception e) {
            MKWorkspaceRuntime.LOGGER.warn("Failed to load workspace export manifest {}", path, e);
            return java.util.Optional.empty();
        }
    }
}
