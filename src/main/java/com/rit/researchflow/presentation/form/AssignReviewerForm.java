package com.rit.researchflow.presentation.form;

import jakarta.validation.constraints.NotNull;

public class AssignReviewerForm {

    @NotNull
    private Long facultyId;

    public Long getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(Long facultyId) {
        this.facultyId = facultyId;
    }
}
