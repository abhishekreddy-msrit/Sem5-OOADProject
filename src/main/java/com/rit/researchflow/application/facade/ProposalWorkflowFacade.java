package com.rit.researchflow.application.facade;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import com.rit.researchflow.application.dto.ProposalContentCommand;
import com.rit.researchflow.application.dto.ReviewProgressView;
import com.rit.researchflow.application.event.ProposalWorkflowEvent;
import com.rit.researchflow.application.exception.ApplicationException;
import com.rit.researchflow.domain.notification.NotificationType;
import com.rit.researchflow.domain.proposal.DecisionRecord;
import com.rit.researchflow.domain.proposal.ProposalStatus;
import com.rit.researchflow.domain.proposal.RecommendationType;
import com.rit.researchflow.domain.proposal.ResearchProposal;
import com.rit.researchflow.domain.proposal.Review;
import com.rit.researchflow.domain.proposal.ReviewerAssignment;
import com.rit.researchflow.domain.repository.ResearchProposalRepository;
import com.rit.researchflow.domain.repository.UserRepository;
import com.rit.researchflow.domain.user.Administrator;
import com.rit.researchflow.domain.user.Faculty;
import com.rit.researchflow.domain.user.Student;
import com.rit.researchflow.domain.user.User;

/**
 * Facade: single application entry point for proposal workflow use cases.
 * Controllers should depend on this class rather than repositories or entities directly.
 */
@Service
public class ProposalWorkflowFacade {

    private final ResearchProposalRepository researchProposalRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final EntityManager entityManager;

    public ProposalWorkflowFacade(ResearchProposalRepository researchProposalRepository,
                                  UserRepository userRepository,
                                  ApplicationEventPublisher eventPublisher,
                                  EntityManager entityManager) {
        this.researchProposalRepository = researchProposalRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.entityManager = entityManager;
    }

    @Transactional
    public ResearchProposal createDraft(Long studentId, ProposalContentCommand content) {
        Student student = requireStudent(studentId);
        ResearchProposal proposal = new ResearchProposal(
                student,
                content.title(),
                content.abstractText(),
                content.objectives(),
                content.methodology(),
                content.expectedOutcomes());
        return researchProposalRepository.save(proposal);
    }

    @Transactional
    public ResearchProposal updateContent(Long proposalId, Long studentId, ProposalContentCommand content) {
        ResearchProposal proposal = requireOwnedProposal(proposalId, studentId);
        proposal.updateContent(
                content.title(),
                content.abstractText(),
                content.objectives(),
                content.methodology(),
                content.expectedOutcomes());
        return researchProposalRepository.save(proposal);
    }

    @Transactional
    public ResearchProposal submitProposal(Long proposalId, Long studentId) {
        ResearchProposal proposal = requireOwnedProposal(proposalId, studentId);
        boolean resubmission = proposal.getStatus() == ProposalStatus.REVISION_REQUIRED;
        proposal.submit();
        ResearchProposal saved = researchProposalRepository.save(proposal);

        if (resubmission) {
            publish(saved, NotificationType.PROPOSAL_RESUBMITTED,
                    "Revised proposal \"" + saved.getTitle() + "\" was resubmitted and awaits reassignment.");
        } else {
            publish(saved, NotificationType.PROPOSAL_SUBMITTED,
                    "New proposal \"" + saved.getTitle() + "\" was submitted and awaits reviewer assignment.");
        }
        return saved;
    }

    @Transactional
    public ReviewerAssignment assignReviewer(Long proposalId, Long facultyId, Long adminId) {
        ResearchProposal proposal = requireProposal(proposalId);
        Faculty faculty = requireFaculty(facultyId);
        Administrator admin = requireAdministrator(adminId);
        proposal.assignReviewer(faculty, admin);
        ResearchProposal saved = researchProposalRepository.save(proposal);
        entityManager.flush();
        ReviewerAssignment persistedAssignment = saved.getActiveAssignments().stream()
                .filter(a -> a.belongsTo(faculty) && a.matchesCycle(saved.getRevisionNumber()))
                .findFirst()
                .orElseThrow(() -> new ApplicationException("Failed to persist reviewer assignment"));

        eventPublisher.publishEvent(new ProposalWorkflowEvent(
                saved.getId(),
                saved.getAuthor().getId(),
                faculty.getId(),
                NotificationType.REVIEWER_ASSIGNED,
                "You have been assigned to review proposal \"" + saved.getTitle() + "\"."));
        return persistedAssignment;
    }

    @Transactional
    public Review submitReview(Long proposalId, Long facultyId, Long assignmentId,
                               String comments, RecommendationType recommendation) {
        ResearchProposal proposal = requireProposal(proposalId);
        Faculty faculty = requireFaculty(facultyId);
        ReviewerAssignment assignment = proposal.getAssignments().stream()
                .filter(a -> a.getId() != null && a.getId().equals(assignmentId))
                .findFirst()
                .orElseThrow(() -> new ApplicationException("Reviewer assignment not found"));
        if (!assignment.belongsTo(faculty)) {
            throw new ApplicationException("Assignment does not belong to this faculty member");
        }

        Review review = proposal.addReview(assignment, comments, recommendation);
        ResearchProposal saved = researchProposalRepository.save(proposal);

        eventPublisher.publishEvent(new ProposalWorkflowEvent(
                saved.getId(),
                saved.getAuthor().getId(),
                faculty.getId(),
                NotificationType.REVIEW_SUBMITTED,
                "A review was submitted for proposal \"" + saved.getTitle() + "\"."));
        return review;
    }

    @Transactional
    public DecisionRecord requestRevision(Long proposalId, Long adminId, String note) {
        ResearchProposal proposal = requireProposal(proposalId);
        Administrator admin = requireAdministrator(adminId);
        DecisionRecord decision = proposal.requestRevision(admin, note);
        ResearchProposal saved = researchProposalRepository.save(proposal);
        publish(saved, NotificationType.REVISION_REQUIRED,
                "Revision has been requested for proposal \"" + saved.getTitle() + "\".");
        return decision;
    }

    @Transactional
    public DecisionRecord approveProposal(Long proposalId, Long adminId, String note) {
        ResearchProposal proposal = requireProposal(proposalId);
        Administrator admin = requireAdministrator(adminId);
        DecisionRecord decision = proposal.approve(admin, note);
        ResearchProposal saved = researchProposalRepository.save(proposal);
        publish(saved, NotificationType.PROPOSAL_APPROVED,
                "Proposal \"" + saved.getTitle() + "\" has been approved.");
        return decision;
    }

    @Transactional
    public DecisionRecord rejectProposal(Long proposalId, Long adminId, String note) {
        ResearchProposal proposal = requireProposal(proposalId);
        Administrator admin = requireAdministrator(adminId);
        DecisionRecord decision = proposal.reject(admin, note);
        ResearchProposal saved = researchProposalRepository.save(proposal);
        publish(saved, NotificationType.PROPOSAL_REJECTED,
                "Proposal \"" + saved.getTitle() + "\" has been rejected.");
        return decision;
    }

    @Transactional(readOnly = true)
    public ResearchProposal getProposal(Long proposalId) {
        return initializeForView(requireProposal(proposalId));
    }

    @Transactional(readOnly = true)
    public ResearchProposal getOwnedProposal(Long proposalId, Long studentId) {
        return initializeForView(requireOwnedProposal(proposalId, studentId));
    }

    @Transactional(readOnly = true)
    public ResearchProposal getAssignedProposal(Long proposalId, Long facultyId) {
        ResearchProposal proposal = requireProposal(proposalId);
        Faculty faculty = requireFaculty(facultyId);
        boolean activelyAssignedThisCycle = proposal.getAssignments().stream()
                .anyMatch(a -> a.belongsTo(faculty)
                        && a.isActive()
                        && a.matchesCycle(proposal.getRevisionNumber()));
        if (!activelyAssignedThisCycle) {
            throw new ApplicationException(
                    "You are not actively assigned to this proposal for the current revision cycle");
        }
        return initializeForView(proposal);
    }

    @Transactional(readOnly = true)
    public List<ResearchProposal> listProposalsForStudent(Long studentId) {
        requireStudent(studentId);
        return researchProposalRepository.findByAuthorId(studentId).stream()
                .map(this::initializeForList)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ResearchProposal> listAssignedProposalsForFaculty(Long facultyId) {
        requireFaculty(facultyId);
        return researchProposalRepository.findByActiveFacultyAssignee(facultyId).stream()
                .map(this::initializeForList)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ResearchProposal> listAllProposals() {
        return researchProposalRepository.findAll().stream()
                .map(this::initializeForList)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Faculty> listActiveFaculty() {
        List<Faculty> faculty = userRepository.findActiveFaculty();
        faculty.forEach(f -> {
            f.getFullName();
            f.getUsername();
        });
        return faculty;
    }

    @Transactional(readOnly = true)
    public ReviewProgressView getReviewProgress(Long proposalId) {
        ResearchProposal proposal = requireProposal(proposalId);
        List<ReviewProgressView.ReviewerProgressItem> items = new ArrayList<>();
        for (ReviewerAssignment assignment : proposal.getAssignments()) {
            if (!assignment.matchesCycle(proposal.getRevisionNumber())) {
                continue;
            }
            Review review = assignment.getReview();
            items.add(new ReviewProgressView.ReviewerProgressItem(
                    assignment.getFaculty().getId(),
                    assignment.getFaculty().getFullName(),
                    assignment.isActive(),
                    review != null,
                    review != null ? review.getRecommendation() : null));
        }
        return new ReviewProgressView(
                proposal.getId(),
                proposal.getTitle(),
                proposal.getStatus(),
                proposal.getRevisionNumber(),
                items);
    }

    private void publish(ResearchProposal proposal, NotificationType type, String message) {
        eventPublisher.publishEvent(new ProposalWorkflowEvent(
                proposal.getId(),
                proposal.getAuthor().getId(),
                null,
                type,
                message));
    }

    /**
     * Touch associations needed by list screens while the transaction is open
     * (open-in-view is disabled).
     */
    private ResearchProposal initializeForList(ResearchProposal proposal) {
        if (proposal.getAuthor() != null) {
            proposal.getAuthor().getFullName();
        }
        proposal.getStatus();
        proposal.getTitle();
        return proposal;
    }

    /**
     * Touch associations needed by detail screens while the transaction is open
     * (open-in-view is disabled).
     */
    private ResearchProposal initializeForView(ResearchProposal proposal) {
        initializeForList(proposal);
        for (ReviewerAssignment assignment : proposal.getAssignments()) {
            if (assignment.getFaculty() != null) {
                assignment.getFaculty().getFullName();
            }
            Review review = assignment.getReview();
            if (review != null) {
                review.getComments();
                review.getRecommendation();
            }
        }
        for (DecisionRecord decision : proposal.getDecisions()) {
            if (decision.getDecidedBy() != null) {
                decision.getDecidedBy().getFullName();
            }
            decision.getDecisionType();
            decision.getNote();
        }
        return proposal;
    }

    private ResearchProposal requireProposal(Long proposalId) {
        return researchProposalRepository.findById(proposalId)
                .orElseThrow(() -> new ApplicationException("Proposal not found"));
    }

    private ResearchProposal requireOwnedProposal(Long proposalId, Long studentId) {
        ResearchProposal proposal = requireProposal(proposalId);
        if (proposal.getAuthor() == null || !proposal.getAuthor().getId().equals(studentId)) {
            throw new ApplicationException("You can only modify your own proposals");
        }
        return proposal;
    }

    private Student requireStudent(Long id) {
        User user = requireUser(id);
        if (!(user instanceof Student student)) {
            throw new ApplicationException("Student account required");
        }
        return student;
    }

    private Faculty requireFaculty(Long id) {
        User user = requireUser(id);
        if (!(user instanceof Faculty faculty)) {
            throw new ApplicationException("Faculty account required");
        }
        return faculty;
    }

    private Administrator requireAdministrator(Long id) {
        User user = requireUser(id);
        if (!(user instanceof Administrator administrator)) {
            throw new ApplicationException("Administrator account required");
        }
        return administrator;
    }

    private User requireUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApplicationException("User not found"));
    }
}
