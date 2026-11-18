package com.rit.researchflow.domain.user;

import com.rit.researchflow.domain.proposal.ResearchProposal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "students")
@PrimaryKeyJoinColumn(name = "user_id")
public class Student extends User {

    @Column(nullable = false, length = 200)
    private String department;

    @Column(name = "student_identifier", nullable = false, length = 100)
    private String studentIdentifier;

    protected Student() {
        // JPA
    }

    public Student(String username, String passwordHash, String fullName, String email,
                   String department, String studentIdentifier) {
        super(username, passwordHash, fullName, email);
        this.department = department;
        this.studentIdentifier = studentIdentifier;
    }

    @Override
    public String getRoleName() {
        return "STUDENT";
    }

    public String getDepartment() {
        return department;
    }

    public String getStudentIdentifier() {
        return studentIdentifier;
    }

    public boolean owns(ResearchProposal proposal) {
        return proposal != null && proposal.getAuthor() != null && proposal.getAuthor().equals(this);
    }
}
