package com.rit.researchflow.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.rit.researchflow.domain.proposal.ResearchProposal;
import com.rit.researchflow.domain.repository.ResearchProposalRepository;

@Repository
public class ResearchProposalRepositoryAdapter implements ResearchProposalRepository {

    private final SpringDataResearchProposalJpaRepository jpaRepository;

    public ResearchProposalRepositoryAdapter(SpringDataResearchProposalJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ResearchProposal save(ResearchProposal proposal) {
        return jpaRepository.save(proposal);
    }

    @Override
    public Optional<ResearchProposal> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<ResearchProposal> findByAuthorId(Long authorId) {
        return jpaRepository.findByAuthorId(authorId);
    }

    @Override
    public List<ResearchProposal> findByActiveFacultyAssignee(Long facultyId) {
        return jpaRepository.findByActiveFacultyAssignee(facultyId);
    }

    @Override
    public List<ResearchProposal> findAll() {
        return jpaRepository.findAll();
    }
}
