package com.rit.researchflow.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.rit.researchflow.application.dto.ReviewProgressView;
import com.rit.researchflow.application.exception.ApplicationException;
import com.rit.researchflow.application.facade.ProposalWorkflowFacade;
import com.rit.researchflow.domain.proposal.ResearchProposal;
import com.rit.researchflow.presentation.form.AssignReviewerForm;
import com.rit.researchflow.presentation.form.DecisionForm;
import com.rit.researchflow.presentation.support.CurrentUser;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProposalWorkflowFacade facade;

    public AdminController(ProposalWorkflowFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/proposals")
    public String dashboard(Model model) {
        model.addAttribute("proposals", facade.listAllProposals());
        return "admin/dashboard";
    }

    @GetMapping("/proposals/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        ResearchProposal proposal = facade.getProposal(id);
        ReviewProgressView progress = facade.getReviewProgress(id);
        model.addAttribute("proposal", proposal);
        model.addAttribute("progress", progress);
        model.addAttribute("facultyList", facade.listActiveFaculty());
        if (!model.containsAttribute("assignReviewerForm")) {
            model.addAttribute("assignReviewerForm", new AssignReviewerForm());
        }
        if (!model.containsAttribute("decisionForm")) {
            model.addAttribute("decisionForm", new DecisionForm());
        }
        return "admin/proposal-detail";
    }

    @PostMapping("/proposals/{id}/assign")
    public String assign(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("assignReviewerForm") AssignReviewerForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return detailWithForms(id, model, form, new DecisionForm());
        }
        facade.assignReviewer(id, form.getFacultyId(), CurrentUser.id());
        redirectAttributes.addFlashAttribute("successMessage", "Reviewer assigned.");
        return "redirect:/admin/proposals/" + id;
    }

    @PostMapping("/proposals/{id}/decision")
    public String decide(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("decisionForm") DecisionForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return detailWithForms(id, model, new AssignReviewerForm(), form);
        }

        String action = form.getAction() == null ? "" : form.getAction().trim().toUpperCase();
        switch (action) {
            case "APPROVE" -> facade.approveProposal(id, CurrentUser.id(), form.getNote());
            case "REJECT" -> facade.rejectProposal(id, CurrentUser.id(), form.getNote());
            case "REVISION" -> facade.requestRevision(id, CurrentUser.id(), form.getNote());
            default -> throw new ApplicationException("Unknown decision action");
        }

        redirectAttributes.addFlashAttribute("successMessage", "Decision recorded.");
        return "redirect:/admin/proposals/" + id;
    }

    private String detailWithForms(Long id, Model model, AssignReviewerForm assignForm, DecisionForm decisionForm) {
        ResearchProposal proposal = facade.getProposal(id);
        model.addAttribute("proposal", proposal);
        model.addAttribute("progress", facade.getReviewProgress(id));
        model.addAttribute("facultyList", facade.listActiveFaculty());
        model.addAttribute("assignReviewerForm", assignForm);
        model.addAttribute("decisionForm", decisionForm);
        return "admin/proposal-detail";
    }
}
