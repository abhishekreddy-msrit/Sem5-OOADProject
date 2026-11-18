package com.rit.researchflow.application.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import com.rit.researchflow.application.dto.ProposalContentCommand;
import com.rit.researchflow.application.facade.ProposalWorkflowFacade;
import com.rit.researchflow.domain.notification.NotificationType;
import com.rit.researchflow.domain.proposal.ResearchProposal;
import com.rit.researchflow.domain.repository.NotificationRepository;
import com.rit.researchflow.domain.repository.UserRepository;
import com.rit.researchflow.domain.user.Administrator;
import com.rit.researchflow.domain.user.Student;

@SpringBootTest
@ActiveProfiles("test")
class NotificationObserverTest {

    @Autowired
    private ProposalWorkflowFacade facade;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Student student;
    private Administrator admin;

    @BeforeEach
    void setUp() {
        long n = System.nanoTime();
        student = (Student) userRepository.save(new Student(
                "obs_s_" + n, passwordEncoder.encode("password123"), "Observer Student",
                "obs.s@example.com", "CSE", "USN-O"));
        admin = (Administrator) userRepository.save(new Administrator(
                "obs_a_" + n, passwordEncoder.encode("password123"), "Observer Admin",
                "obs.a@example.com", "Office"));
    }

    @Test
    void createsNotificationAfterSuccessfulCommit() {
        ResearchProposal proposal = transactionTemplate.execute(status ->
                facade.createDraft(student.getId(), content("Observer Commit Notification Study")));

        int before = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(admin.getId()).size();

        transactionTemplate.executeWithoutResult(status ->
                facade.submitProposal(proposal.getId(), student.getId()));

        var notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(admin.getId());
        assertTrue(notifications.size() > before);
        assertEquals(NotificationType.PROPOSAL_SUBMITTED, notifications.get(0).getType());
    }

    @Test
    void doesNotCreateNotificationWhenTransactionRollsBack() {
        ResearchProposal proposal = transactionTemplate.execute(status ->
                facade.createDraft(student.getId(), content("Observer Rollback Notification Study")));

        int before = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(admin.getId()).size();

        try {
            transactionTemplate.executeWithoutResult(status -> {
                facade.submitProposal(proposal.getId(), student.getId());
                status.setRollbackOnly();
            });
        } catch (Exception ignored) {
            // rollback path should not require an exception
        }

        int after = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(admin.getId()).size();
        assertEquals(before, after);
    }

    private ProposalContentCommand content(String title) {
        return new ProposalContentCommand(
                title,
                "Observer tests verify notification persistence only after successful transaction commit.",
                "Confirm AFTER_COMMIT listener behaviour for ResearchFlow workflow events.",
                "Submit a proposal inside controlled transactions and inspect notification side effects.",
                "Expect notifications only when the originating workflow transaction commits successfully.");
    }
}
