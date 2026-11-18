package com.rit.researchflow.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.rit.researchflow.application.notification.NotificationInboxService;
import com.rit.researchflow.presentation.support.CurrentUser;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationInboxService notificationInboxService;

    public NotificationController(NotificationInboxService notificationInboxService) {
        this.notificationInboxService = notificationInboxService;
    }

    @GetMapping
    public String inbox(Model model) {
        model.addAttribute("notifications", notificationInboxService.listForUser(CurrentUser.id()));
        return "notifications/inbox";
    }

    @PostMapping("/{id}/read")
    public String markRead(@PathVariable("id") Long id) {
        notificationInboxService.markRead(id, CurrentUser.id());
        return "redirect:/notifications";
    }
}
