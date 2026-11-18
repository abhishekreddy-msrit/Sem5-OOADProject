package com.rit.researchflow.presentation.advice;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.rit.researchflow.application.exception.ApplicationException;
import com.rit.researchflow.domain.exception.DomainValidationException;
import com.rit.researchflow.domain.exception.IllegalProposalTransitionException;

@ControllerAdvice
public class GlobalUiExceptionHandler {

    @ExceptionHandler({
            ApplicationException.class,
            DomainValidationException.class,
            IllegalProposalTransitionException.class
    })
    public String handleDomainErrors(RuntimeException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/simple";
    }
}
