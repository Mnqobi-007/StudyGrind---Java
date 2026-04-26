package com.studygrind.controller;

import com.studygrind.security.UserPrincipal;
import com.studygrind.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @GetMapping
    public ResponseEntity<List<NotificationService.Notification>> getNotifications(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(notificationService.getUserNotifications(currentUser.getId()));
    }
    
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Integer>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        Map<String, Integer> response = new HashMap<>();
        response.put("count", notificationService.getUnreadCount(currentUser.getId()));
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/mark-read/{notificationId}")
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId,
                                         @AuthenticationPrincipal UserPrincipal currentUser) {
        notificationService.markAsRead(currentUser.getId(), notificationId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearNotifications(@AuthenticationPrincipal UserPrincipal currentUser) {
        notificationService.clearNotifications(currentUser.getId());
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
}