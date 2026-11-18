package com.rit.researchflow.presentation.form;

import com.rit.researchflow.domain.proposal.RecommendationType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReviewForm {

    @NotNull
    private Long assignmentId;

    @NotBlank
    @Size(min = 10, max = 4000)
    private String comments;

    @NotNull
    private RecommendationType recommendation;

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public RecommendationType getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(RecommendationType recommendation) {
        this.recommendation = recommendation;
    }
}
