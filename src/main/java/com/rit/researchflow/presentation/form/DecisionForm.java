package com.rit.researchflow.presentation.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DecisionForm {

    @NotBlank
    private String action;

    @Size(max = 2000)
    private String note;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
