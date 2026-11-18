package com.rit.researchflow.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "faculty")
@PrimaryKeyJoinColumn(name = "user_id")
public class Faculty extends User {

    @Column(nullable = false, length = 200)
    private String department;

    @Column(nullable = false, length = 200)
    private String designation;

    protected Faculty() {
        // JPA
    }

    public Faculty(String username, String passwordHash, String fullName, String email,
                   String department, String designation) {
        super(username, passwordHash, fullName, email);
        this.department = department;
        this.designation = designation;
    }

    @Override
    public String getRoleName() {
        return "FACULTY";
    }

    public String getDepartment() {
        return department;
    }

    public String getDesignation() {
        return designation;
    }
}
