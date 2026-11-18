package com.rit.researchflow.presentation.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.rit.researchflow.application.exception.ApplicationException;
import com.rit.researchflow.application.facade.ProposalWorkflowFacade;
import com.rit.researchflow.domain.proposal.ResearchProposal;
import com.rit.researchflow.domain.proposal.ReviewerAssignment;
import com.rit.researchflow.presentation.form.ReviewForm;
import com.rit.researchflow.presentation.support.CurrentUser;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/faculty")
public class FacultyController {

    private final ProposalWorkflowFacade facade;

    public FacultyController(ProposalWorkflowFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/assignments")
    public String dashboard(Model model) {
        model.addAttribute("proposals", facade.listAssignedProposalsForFaculty(CurrentUser.id()));
        return "faculty/dashboard";
    }

    @GetMapping("/proposals/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        ResearchProposal proposal = facade.getAssignedProposal(id, CurrentUser.id());
        Optional<ReviewerAssignment> activeAssignment = findActiveAssignment(proposal);
        model.addAttribute("proposal", proposal);
        model.addAttribute("activeAssignment", activeAssignment.orElse(null));
        model.addAttribute("canReview", activeAssignment.isPresent() && activeAssignment.get().getReview() == null);

        if (!model.containsAttribute("reviewForm") && activeAssignment.isPresent()) {
            ReviewForm form = new ReviewForm();
            form.setAssignmentId(activeAssignment.get().getId());
            model.addAttribute("reviewForm", form);
        }
        return "faculty/proposal-detail";
    }

    @PostMapping("/proposals/{id}/reviews")
    public String submitReview(@PathVariable("id") Long id,
                               @Valid @ModelAttribute("reviewForm") ReviewForm form,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            ResearchProposal proposal = facade.getAssignedProposal(id, CurrentUser.id());
            model.addAttribute("proposal", proposal);
            model.addAttribute("activeAssignment", findActiveAssignment(proposal).orElse(null));
            model.addAttribute("canReview", true);
            return "faculty/proposal-detail";
        }

        facade.submitReview(
                id,
                CurrentUser.id(),
                form.getAssignmentId(),
                form.getComments(),
                form.getRecommendation());
        redirectAttributes.addFlashAttribute("successMessage", "Review submitted.");
        return "redirect:/faculty/proposals/" + id;
    }

    private Optional<ReviewerAssignment> findActiveAssignment(ResearchProposal proposal) {
        Long facultyId = CurrentUser.id();
        return proposal.getActiveAssignments().stream()
                .filter(a -> a.getFaculty() != null && facultyId.equals(a.getFaculty().getId()))
                .filter(a -> a.matchesCycle(proposal.getRevisionNumber()))
                .findFirst();
    }
}
