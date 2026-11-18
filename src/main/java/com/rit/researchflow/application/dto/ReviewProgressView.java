package com.rit.researchflow.application.dto;

import java.util.List;

import com.rit.researchflow.domain.proposal.ProposalStatus;
import com.rit.researchflow.domain.proposal.RecommendationType;

public record ReviewProgressView(
        Long proposalId,
        String title,
        ProposalStatus status,
        int revisionNumber,
        List<ReviewerProgressItem> reviewers
) {
    public record ReviewerProgressItem(
            Long facultyId,
            String facultyName,
            boolean active,
            boolean reviewSubmitted,
            RecommendationType recommendation
    ) {
    }
}
