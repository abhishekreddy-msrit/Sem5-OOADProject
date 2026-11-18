package com.rit.researchflow.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "administrators")
@PrimaryKeyJoinColumn(name = "user_id")
public class Administrator extends User {

    @Column(name = "office_label", length = 200)
    private String officeLabel;

    protected Administrator() {
        // JPA
    }

    public Administrator(String username, String passwordHash, String fullName, String email, String officeLabel) {
        super(username, passwordHash, fullName, email);
        this.officeLabel = officeLabel;
    }

    @Override
    public String getRoleName() {
        return "ADMINISTRATOR";
    }

    public String getOfficeLabel() {
        return officeLabel;
    }
}
