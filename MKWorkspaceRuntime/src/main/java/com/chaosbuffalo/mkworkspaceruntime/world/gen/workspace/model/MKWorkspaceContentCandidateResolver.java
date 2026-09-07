package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Resolves workspace pieces into the slot -> family -> candidate hierarchy used by preview and runtime export.
 */
public final class MKWorkspaceContentCandidateResolver {
    private MKWorkspaceContentCandidateResolver() {
    }

    public static Resolution resolveWorkspacePieces(List<MKWorkspacePieceDefinition> pieces) {
        return resolve(pieces.stream().map(ContentPiece::from).toList());
    }

    public static Resolution resolve(List<ContentPiece> pieces) {
        LinkedHashMap<String, List<ContentPiece>> piecesByFamily = new LinkedHashMap<>();
        for (ContentPiece piece : pieces) {
            if (!piece.purpose().placeable()) {
                continue;
            }
            String familyId = piece.familyId();
            if (familyId.isBlank()) {
                continue;
            }
            piecesByFamily.computeIfAbsent(familyId, ignored -> new ArrayList<>()).add(piece);
        }

        ArrayList<String> diagnostics = new ArrayList<>();
        ArrayList<ResolvedFamily> families = new ArrayList<>();
        for (Map.Entry<String, List<ContentPiece>> entry : piecesByFamily.entrySet()) {
            families.add(resolveFamily(entry.getKey(), entry.getValue(), diagnostics));
        }
        return new Resolution(List.copyOf(families), List.copyOf(diagnostics));
    }

    private static ResolvedFamily resolveFamily(String familyId, List<ContentPiece> sourcePieces,
                                                List<String> diagnostics) {
        List<ContentPiece> pieces = sourcePieces.stream()
                .sorted(Comparator.comparing(ContentPiece::pieceName))
                .toList();
        ContentPiece metadataSource = pieces.stream()
                .filter(piece -> piece.purpose().canonical())
                .findFirst()
                .orElse(pieces.getFirst());
        String topologySlotId = metadataSource.topologySlotId();
        int familyWeight = metadataSource.familyWeight();
        boolean familyEnabled = metadataSource.familyEnabled();

        LinkedHashSet<String> slotIds = pieces.stream()
                .map(ContentPiece::topologySlotId)
                .filter(value -> !value.isBlank())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        if (slotIds.size() > 1) {
            diagnostics.add("family " + familyId + " references multiple topology slots " + slotIds);
        }
        if (pieces.stream().anyMatch(piece -> piece.familyWeight() != familyWeight)) {
            diagnostics.add("family " + familyId + " declares inconsistent family weights; using " +
                    familyWeight + " from " + metadataSource.pieceName());
        }
        if (pieces.stream().anyMatch(piece -> piece.familyEnabled() != familyEnabled)) {
            diagnostics.add("family " + familyId + " declares inconsistent enabled state; using " +
                    familyEnabled + " from " + metadataSource.pieceName());
        }

        List<ContentPiece> variants = pieces.stream()
                .filter(piece -> piece.purpose().variant())
                .filter(ContentPiece::variantEnabled)
                .toList();
        List<ContentPiece> canonicals = pieces.stream()
                .filter(piece -> piece.purpose().canonical())
                .filter(ContentPiece::variantEnabled)
                .toList();
        if (canonicals.size() > 1) {
            diagnostics.add("family " + familyId + " defines multiple canonical templates " +
                    canonicals.stream().map(ContentPiece::pieceName).toList());
        }

        boolean canonicalFallback = variants.isEmpty() && !canonicals.isEmpty();
        List<ResolvedCandidate> candidates;
        if (!familyEnabled) {
            candidates = List.of();
        } else if (!variants.isEmpty()) {
            candidates = variants.stream().map(piece -> ResolvedCandidate.from(piece, false)).toList();
        } else if (!canonicals.isEmpty()) {
            candidates = List.of(ResolvedCandidate.from(canonicals.getFirst(), true));
        } else {
            candidates = List.of();
            diagnostics.add("family " + familyId + " has no placeable canonical or variants");
        }

        return new ResolvedFamily(familyId, topologySlotId, familyWeight, familyEnabled,
                canonicalFallback, candidates);
    }

    /**
     * Flattens two-stage family and candidate weights without making a family more likely merely because it
     * owns more variants.
     */
    public static List<WeightedCandidate> flatten(List<ResolvedFamily> families) {
        return flatten(families, ignored -> true);
    }

    public static List<WeightedCandidate> flatten(List<ResolvedFamily> families,
                                                   Predicate<ResolvedCandidate> candidateFilter) {
        List<ResolvedFamily> eligibleFamilies = families.stream()
                .filter(ResolvedFamily::enabled)
                .map(family -> family.withCandidates(family.candidates().stream()
                        .filter(candidateFilter)
                        .toList()))
                .filter(family -> !family.candidates().isEmpty())
                .toList();
        if (eligibleFamilies.isEmpty()) {
            return List.of();
        }

        LinkedHashMap<String, BigInteger> candidateTotals = new LinkedHashMap<>();
        BigInteger commonDenominator = BigInteger.ONE;
        for (ResolvedFamily family : eligibleFamilies) {
            BigInteger total = family.candidates().stream()
                    .map(candidate -> BigInteger.valueOf(candidate.weight()))
                    .reduce(BigInteger.ZERO, BigInteger::add);
            candidateTotals.put(family.familyId(), total);
            commonDenominator = lcm(commonDenominator, total);
        }

        ArrayList<PendingWeight> pending = new ArrayList<>();
        BigInteger commonFactor = BigInteger.ZERO;
        for (ResolvedFamily family : eligibleFamilies) {
            BigInteger familyScale = commonDenominator.divide(candidateTotals.get(family.familyId()))
                    .multiply(BigInteger.valueOf(family.weight()));
            for (ResolvedCandidate candidate : family.candidates()) {
                BigInteger weight = familyScale.multiply(BigInteger.valueOf(candidate.weight()));
                pending.add(new PendingWeight(family, candidate, weight));
                commonFactor = commonFactor.equals(BigInteger.ZERO) ? weight : commonFactor.gcd(weight);
            }
        }

        ArrayList<WeightedCandidate> result = new ArrayList<>();
        for (PendingWeight entry : pending) {
            BigInteger reduced = entry.weight().divide(commonFactor);
            if (reduced.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
                throw new IllegalStateException("resolved workspace pool weight exceeds integer range for family " +
                        entry.family().familyId() + " candidate " + entry.candidate().pieceName());
            }
            result.add(new WeightedCandidate(
                    entry.family().familyId(),
                    entry.family().topologySlotId(),
                    entry.candidate().pieceName(),
                    entry.candidate().baseName(),
                    entry.candidate().variantId(),
                    entry.candidate().purpose(),
                    reduced.intValueExact(),
                    entry.candidate().canonicalFallback()
            ));
        }
        return List.copyOf(result);
    }

    private static BigInteger lcm(BigInteger left, BigInteger right) {
        return left.divide(left.gcd(right)).multiply(right);
    }

    public record ContentPiece(
            String pieceName,
            String baseName,
            String roleId,
            int legacyVariantIndex,
            Map<String, String> tags
    ) {
        public ContentPiece {
            pieceName = pieceName == null ? "" : pieceName;
            baseName = baseName == null || baseName.isBlank() ? pieceName : baseName;
            roleId = roleId == null ? "" : roleId;
            tags = Map.copyOf(tags == null ? Map.of() : tags);
        }

        public static ContentPiece from(MKWorkspacePieceDefinition piece) {
            return new ContentPiece(
                    piece.pieceName(),
                    piece.tags().getOrDefault("workspace_base_name", piece.pieceName()),
                    piece.roleId(),
                    piece.variantIndex(),
                    piece.tags()
            );
        }

        public MKWorkspaceTemplatePurpose purpose() {
            return MKWorkspaceContentSelectionTags.purpose(tags, legacyVariantIndex);
        }

        public String familyId() {
            return MKWorkspaceContentSelectionTags.familyId(pieceName, tags);
        }

        public String topologySlotId() {
            return MKWorkspaceContentSelectionTags.topologySlotId(roleId, tags);
        }

        public String variantId() {
            return MKWorkspaceContentSelectionTags.variantId(pieceName, tags);
        }

        public int familyWeight() {
            return MKWorkspaceContentSelectionTags.familyWeight(tags);
        }

        public boolean familyEnabled() {
            return MKWorkspaceContentSelectionTags.familyEnabled(tags);
        }

        public int variantWeight() {
            return MKWorkspaceContentSelectionTags.variantWeight(tags);
        }

        public boolean variantEnabled() {
            return MKWorkspaceContentSelectionTags.variantEnabled(tags);
        }
    }

    public record ResolvedCandidate(
            String pieceName,
            String baseName,
            String variantId,
            int weight,
            MKWorkspaceTemplatePurpose purpose,
            boolean canonicalFallback
    ) {
        private static ResolvedCandidate from(ContentPiece piece, boolean canonicalFallback) {
            return new ResolvedCandidate(
                    piece.pieceName(),
                    piece.baseName(),
                    piece.variantId(),
                    canonicalFallback ? 1 : piece.variantWeight(),
                    piece.purpose(),
                    canonicalFallback
            );
        }
    }

    public record ResolvedFamily(
            String familyId,
            String topologySlotId,
            int weight,
            boolean enabled,
            boolean canonicalFallback,
            List<ResolvedCandidate> candidates
    ) {
        public ResolvedFamily {
            weight = Math.max(1, weight);
            candidates = List.copyOf(candidates);
        }

        private ResolvedFamily withCandidates(List<ResolvedCandidate> candidates) {
            return new ResolvedFamily(familyId, topologySlotId, weight, enabled,
                    canonicalFallback && candidates.stream().anyMatch(ResolvedCandidate::canonicalFallback),
                    candidates);
        }
    }

    public record WeightedCandidate(
            String familyId,
            String topologySlotId,
            String pieceName,
            String baseName,
            String variantId,
            MKWorkspaceTemplatePurpose purpose,
            int effectiveWeight,
            boolean canonicalFallback
    ) {
    }

    public record Resolution(List<ResolvedFamily> families, List<String> diagnostics) {
        public Resolution {
            families = List.copyOf(families);
            diagnostics = List.copyOf(diagnostics);
        }

        public List<ResolvedFamily> familiesForSlot(String topologySlotId) {
            return families.stream()
                    .filter(family -> family.topologySlotId().equals(topologySlotId))
                    .toList();
        }

        public ResolvedFamily family(String familyId) {
            return families.stream()
                    .filter(family -> family.familyId().equals(familyId))
                    .findFirst()
                    .orElse(null);
        }
    }

    private record PendingWeight(ResolvedFamily family, ResolvedCandidate candidate, BigInteger weight) {
    }
}
