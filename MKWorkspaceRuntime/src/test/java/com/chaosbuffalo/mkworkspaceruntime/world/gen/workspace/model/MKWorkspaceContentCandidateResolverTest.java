package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MKWorkspaceContentCandidateResolverTest {
    @Test
    void canonicalIsRuntimeFallbackWhenFamilyHasNoVariants() {
        var resolution = MKWorkspaceContentCandidateResolver.resolve(List.of(
                piece("gazebo", "gazebo", "platform", MKWorkspaceTemplatePurpose.FAMILY_CANONICAL,
                        3, 1, true, 0)
        ));

        var family = resolution.family("gazebo");
        assertTrue(family.canonicalFallback());
        assertEquals(List.of("gazebo"), family.candidates().stream().map(
                MKWorkspaceContentCandidateResolver.ResolvedCandidate::pieceName).toList());
    }

    @Test
    void enabledVariantsReplaceCanonicalForPreviewAndRuntime() {
        var resolution = MKWorkspaceContentCandidateResolver.resolve(List.of(
                piece("gazebo", "gazebo", "platform", MKWorkspaceTemplatePurpose.FAMILY_CANONICAL,
                        1, 1, true, 0),
                piece("gazebo_quiet", "gazebo", "platform", MKWorkspaceTemplatePurpose.FAMILY_VARIANT,
                        1, 1, true, 1),
                piece("gazebo_occupied", "gazebo", "platform", MKWorkspaceTemplatePurpose.FAMILY_VARIANT,
                        1, 2, true, 2)
        ));

        var family = resolution.family("gazebo");
        assertFalse(family.canonicalFallback());
        assertEquals(List.of("gazebo_occupied", "gazebo_quiet"), family.candidates().stream()
                .map(MKWorkspaceContentCandidateResolver.ResolvedCandidate::pieceName)
                .toList());
    }

    @Test
    void disabledVariantsDoNotPreventCanonicalFallback() {
        var canonical = piece("fountain", "fountain", "platform",
                MKWorkspaceTemplatePurpose.FAMILY_CANONICAL, 1, 1, true, 0);
        LinkedHashMap<String, String> disabledTags = new LinkedHashMap<>(piece(
                "fountain_guarded", "fountain", "platform", MKWorkspaceTemplatePurpose.FAMILY_VARIANT,
                1, 1, true, 1).tags());
        disabledTags.put(MKWorkspaceContentSelectionTags.VARIANT_ENABLED, "false");

        var resolution = MKWorkspaceContentCandidateResolver.resolve(List.of(
                canonical,
                new MKWorkspaceContentCandidateResolver.ContentPiece(
                        "fountain_guarded", "fountain", "platform", 1, disabledTags)
        ));

        assertEquals("fountain", resolution.family("fountain").candidates().getFirst().pieceName());
        assertTrue(resolution.family("fountain").canonicalFallback());
    }

    @Test
    void slotScaffoldIsNeverAPlaceableCandidate() {
        var resolution = MKWorkspaceContentCandidateResolver.resolve(List.of(
                piece("platform_template", "platform", "platform",
                        MKWorkspaceTemplatePurpose.SLOT_SCAFFOLD, 1, 1, true, 0)
        ));

        assertTrue(resolution.family("platform").candidates().isEmpty());
        assertTrue(resolution.diagnostics().stream().anyMatch(message -> message.contains("only a slot scaffold")));
    }

    @Test
    void hierarchicalWeightsDoNotRewardFamiliesForHavingMoreVariants() {
        var resolution = MKWorkspaceContentCandidateResolver.resolve(List.of(
                piece("gazebo", "gazebo", "platform", MKWorkspaceTemplatePurpose.FAMILY_CANONICAL,
                        3, 1, true, 0),
                piece("gazebo_quiet", "gazebo", "platform", MKWorkspaceTemplatePurpose.FAMILY_VARIANT,
                        3, 1, true, 1),
                piece("gazebo_occupied", "gazebo", "platform", MKWorkspaceTemplatePurpose.FAMILY_VARIANT,
                        3, 2, true, 2),
                piece("fountain", "fountain", "platform", MKWorkspaceTemplatePurpose.FAMILY_CANONICAL,
                        1, 1, true, 0),
                piece("fountain_guarded", "fountain", "platform", MKWorkspaceTemplatePurpose.FAMILY_VARIANT,
                        1, 1, true, 1)
        ));

        Map<String, Integer> weights = MKWorkspaceContentCandidateResolver.flatten(resolution.families()).stream()
                .collect(java.util.stream.Collectors.toMap(
                        MKWorkspaceContentCandidateResolver.WeightedCandidate::pieceName,
                        MKWorkspaceContentCandidateResolver.WeightedCandidate::effectiveWeight));

        assertEquals(Map.of(
                "gazebo_quiet", 1,
                "gazebo_occupied", 2,
                "fountain_guarded", 1
        ), weights);
    }

    @Test
    void legacyInsertTemplateIsInferredAsScaffoldWhileVariantsRemainPlaceable() {
        Map<String, String> scaffoldTags = Map.of(
                "workspace_piece_kind", "template",
                "workspace_insert_family_id", "platform",
                "workspace_base_name", "platform"
        );
        Map<String, String> variantTags = Map.of(
                "workspace_piece_kind", "instance",
                "workspace_insert_family_id", "platform",
                "workspace_base_name", "platform"
        );

        var resolution = MKWorkspaceContentCandidateResolver.resolve(List.of(
                new MKWorkspaceContentCandidateResolver.ContentPiece(
                        "platform_template", "platform", "platform", 0, scaffoldTags),
                new MKWorkspaceContentCandidateResolver.ContentPiece(
                        "platform_gazebo", "platform", "platform", 1, variantTags)
        ));

        assertEquals(MKWorkspaceTemplatePurpose.SLOT_SCAFFOLD,
                MKWorkspaceContentSelectionTags.purpose(scaffoldTags, 0));
        assertEquals(List.of("platform_gazebo"), resolution.family("platform").candidates().stream()
                .map(MKWorkspaceContentCandidateResolver.ResolvedCandidate::pieceName)
                .toList());
    }

    private static MKWorkspaceContentCandidateResolver.ContentPiece piece(
            String pieceName,
            String familyId,
            String slotId,
            MKWorkspaceTemplatePurpose purpose,
            int familyWeight,
            int variantWeight,
            boolean enabled,
            int legacyVariantIndex
    ) {
        Map<String, String> tags = MKWorkspaceContentSelectionTags.applyFamily(
                Map.of("workspace_base_name", familyId), slotId, familyId, familyWeight, enabled);
        tags = MKWorkspaceContentSelectionTags.applyTemplate(tags, purpose, pieceName, variantWeight, enabled);
        return new MKWorkspaceContentCandidateResolver.ContentPiece(
                pieceName, familyId, slotId, legacyVariantIndex, tags);
    }
}
