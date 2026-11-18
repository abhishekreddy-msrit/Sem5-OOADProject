package com.rit.researchflow.presentation.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProposalForm {

    @NotBlank
    @Size(min = 5, max = 200)
    private String title;

    @NotBlank
    @Size(min = 50, max = 4000)
    private String abstractText;

    @NotBlank
    @Size(min = 20, max = 4000)
    private String objectives;

    @NotBlank
    @Size(min = 20, max = 4000)
    private String methodology;

    @NotBlank
    @Size(min = 20, max = 4000)
    private String expectedOutcomes;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    public String getObjectives() {
        return objectives;
    }

    public void setObjectives(String objectives) {
        this.objectives = objectives;
    }

    public String getMethodology() {
        return methodology;
    }

    public void setMethodology(String methodology) {
        this.methodology = methodology;
    }

    public String getExpectedOutcomes() {
        return expectedOutcomes;
    }

    public void setExpectedOutcomes(String expectedOutcomes) {
        this.expectedOutcomes = expectedOutcomes;
    }
}
