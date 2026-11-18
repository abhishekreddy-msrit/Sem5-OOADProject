package com.rit.researchflow.presentation.advice;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.rit.researchflow.application.notification.NotificationInboxService;
import com.rit.researchflow.infrastructure.security.ResearchFlowUserDetails;

@ControllerAdvice
public class LayoutModelAdvice {

    private final NotificationInboxService notificationInboxService;

    public LayoutModelAdvice(NotificationInboxService notificationInboxService) {
        this.notificationInboxService = notificationInboxService;
    }

    @ModelAttribute("currentUsername")
    public String currentUsername() {
        ResearchFlowUserDetails details = currentDetails();
        return details == null ? null : details.getUsername();
    }

    @ModelAttribute("currentRole")
    public String currentRole() {
        ResearchFlowUserDetails details = currentDetails();
        return details == null ? null : details.getRoleName();
    }

    @ModelAttribute("unreadNotificationCount")
    public Long unreadNotificationCount() {
        ResearchFlowUserDetails details = currentDetails();
        if (details == null) {
            return 0L;
        }
        return notificationInboxService.countUnread(details.getUserId());
    }

    private ResearchFlowUserDetails currentDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof ResearchFlowUserDetails details)) {
            return null;
        }
        return details;
    }
}
