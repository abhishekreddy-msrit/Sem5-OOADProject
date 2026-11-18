package com.rit.researchflow.domain.repository;

import java.util.List;
import java.util.Optional;

import com.rit.researchflow.domain.proposal.ResearchProposal;

public interface ResearchProposalRepository {

    ResearchProposal save(ResearchProposal proposal);

    Optional<ResearchProposal> findById(Long id);

    List<ResearchProposal> findByAuthorId(Long authorId);

    List<ResearchProposal> findByActiveFacultyAssignee(Long facultyId);

    List<ResearchProposal> findAll();
}
