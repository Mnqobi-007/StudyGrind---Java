package com.studygrind.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {

    private final Map<Long, List<Notification>> userNotifications = new ConcurrentHashMap<>();
    private final Map<Long, List<Notification>> courseNotifications = new ConcurrentHashMap<>();

    public void sendNotification(Long userId, String message, String type) {
        Notification notification = new Notification(message, type, System.currentTimeMillis(), false);
        userNotifications.computeIfAbsent(userId, k -> new ArrayList<>()).add(notification);

        // Limit to last 50 notifications
        List<Notification> list = userNotifications.get(userId);
        if (list.size() > 50) {
            list.remove(0);
        }
    }

    public void sendSubmissionUpdate(Long courseId, String assignmentTitle, String studentName) {
        String message = "New submission from " + studentName + " for " + assignmentTitle;
        Notification notification = new Notification(message, "submission", System.currentTimeMillis(), false);
        courseNotifications.computeIfAbsent(courseId, k -> new ArrayList<>()).add(notification);
    }

    public void sendAnnouncement(Long courseId, String title, String content) {
        Notification notification = new Notification(title + ": " + content, "announcement", System.currentTimeMillis(), false);
        courseNotifications.computeIfAbsent(courseId, k -> new ArrayList<>()).add(notification);
    }

    public List<Notification> getUserNotifications(Long userId) {
        return userNotifications.getOrDefault(userId, new ArrayList<>());
    }

    public List<Notification> getCourseNotifications(Long courseId) {
        return courseNotifications.getOrDefault(courseId, new ArrayList<>());
    }

    public void markAsRead(Long userId, Long notificationId) {
        List<Notification> notifications = userNotifications.get(userId);
        if (notifications != null && notificationId < notifications.size()) {
            notifications.get(notificationId.intValue()).setRead(true);
        }
    }

    public void markAsReadByIndex(Long userId, int index) {
        List<Notification> notifications = userNotifications.get(userId);
        if (notifications != null && index < notifications.size()) {
            notifications.get(index).setRead(true);
        }
    }

    public int getUnreadCount(Long userId) {
        List<Notification> notifications = userNotifications.get(userId);
        if (notifications == null) return 0;
        return (int) notifications.stream().filter(n -> !n.isRead()).count();
    }

    public void clearNotifications(Long userId) {
        userNotifications.remove(userId);
    }

    public static class Notification {
        private String message;
        private String type;
        private long timestamp;
        private boolean read;

        public Notification(String message, String type, long timestamp, boolean read) {
            this.message = message;
            this.type = type;
            this.timestamp = timestamp;
            this.read = read;
        }

        public String getMessage() { return message; }
        public String getType() { return type; }
        public long getTimestamp() { return timestamp; }
        public boolean isRead() { return read; }
        public void setRead(boolean read) { this.read = read; }
    }
}