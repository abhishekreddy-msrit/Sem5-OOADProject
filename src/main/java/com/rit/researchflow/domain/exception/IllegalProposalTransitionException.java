package com.rit.researchflow.domain.exception;

import com.rit.researchflow.domain.proposal.ProposalStatus;

public class IllegalProposalTransitionException extends RuntimeException {

    public IllegalProposalTransitionException(String message) {
        super(message);
    }

    public IllegalProposalTransitionException(ProposalStatus from, String action) {
        super("Cannot " + action + " when proposal status is " + from);
    }
}
