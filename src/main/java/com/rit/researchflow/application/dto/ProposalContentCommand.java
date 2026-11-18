package com.rit.researchflow.application.dto;

public record ProposalContentCommand(
        String title,
        String abstractText,
        String objectives,
        String methodology,
        String expectedOutcomes
) {
}
