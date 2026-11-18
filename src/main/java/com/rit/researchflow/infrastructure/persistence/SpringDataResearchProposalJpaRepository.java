package com.rit.researchflow.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rit.researchflow.domain.proposal.ResearchProposal;

public interface SpringDataResearchProposalJpaRepository extends JpaRepository<ResearchProposal, Long> {

    List<ResearchProposal> findByAuthorId(Long authorId);

    @Query("""
            select distinct p from ResearchProposal p
            join p.assignments a
            where a.faculty.id = :facultyId and a.active = true
            """)
    List<ResearchProposal> findByActiveFacultyAssignee(@Param("facultyId") Long facultyId);
}
