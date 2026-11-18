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

import com.rit.researchflow.application.dto.ProposalContentCommand;
import com.rit.researchflow.application.facade.ProposalWorkflowFacade;
import com.rit.researchflow.domain.proposal.ProposalStatus;
import com.rit.researchflow.domain.proposal.ResearchProposal;
import com.rit.researchflow.presentation.form.ProposalForm;
import com.rit.researchflow.presentation.support.CurrentUser;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/student")
public class StudentController {

    private final ProposalWorkflowFacade facade;

    public StudentController(ProposalWorkflowFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/proposals")
    public String dashboard(Model model) {
        model.addAttribute("proposals", facade.listProposalsForStudent(CurrentUser.id()));
        return "student/dashboard";
    }

    @GetMapping("/proposals/new")
    public String createForm(Model model) {
        model.addAttribute("proposalForm", new ProposalForm());
        model.addAttribute("mode", "create");
        return "student/proposal-form";
    }

    @PostMapping("/proposals")
    public String create(@Valid @ModelAttribute("proposalForm") ProposalForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "create");
            return "student/proposal-form";
        }
        ResearchProposal created = facade.createDraft(CurrentUser.id(), toCommand(form));
        redirectAttributes.addFlashAttribute("successMessage", "Draft proposal created.");
        return "redirect:/student/proposals/" + created.getId();
    }

    @GetMapping("/proposals/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        ResearchProposal proposal = facade.getOwnedProposal(id, CurrentUser.id());
        model.addAttribute("proposal", proposal);
        model.addAttribute("editable", proposal.isEditable());
        model.addAttribute("canSubmit",
                proposal.getStatus() == ProposalStatus.DRAFT
                        || proposal.getStatus() == ProposalStatus.REVISION_REQUIRED);
        return "student/proposal-detail";
    }

    @GetMapping("/proposals/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        ResearchProposal proposal = facade.getOwnedProposal(id, CurrentUser.id());
        if (!proposal.isEditable()) {
            return "redirect:/student/proposals/" + id;
        }
        ProposalForm form = new ProposalForm();
        form.setTitle(proposal.getTitle());
        form.setAbstractText(proposal.getAbstractText());
        form.setObjectives(proposal.getObjectives());
        form.setMethodology(proposal.getMethodology());
        form.setExpectedOutcomes(proposal.getExpectedOutcomes());
        model.addAttribute("proposalForm", form);
        model.addAttribute("mode", "edit");
        model.addAttribute("proposalId", id);
        return "student/proposal-form";
    }

    @PostMapping("/proposals/{id}/edit")
    public String edit(@PathVariable("id") Long id,
                       @Valid @ModelAttribute("proposalForm") ProposalForm form,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("proposalId", id);
            return "student/proposal-form";
        }
        facade.updateContent(id, CurrentUser.id(), toCommand(form));
        redirectAttributes.addFlashAttribute("successMessage", "Proposal updated.");
        return "redirect:/student/proposals/" + id;
    }

    @PostMapping("/proposals/{id}/submit")
    public String submit(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        facade.submitProposal(id, CurrentUser.id());
        redirectAttributes.addFlashAttribute("successMessage", "Proposal submitted.");
        return "redirect:/student/proposals/" + id;
    }

    private ProposalContentCommand toCommand(ProposalForm form) {
        return new ProposalContentCommand(
                form.getTitle(),
                form.getAbstractText(),
                form.getObjectives(),
                form.getMethodology(),
                form.getExpectedOutcomes());
    }
}
