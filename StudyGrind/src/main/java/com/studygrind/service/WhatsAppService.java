package com.studygrind.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WhatsAppService {
    
    @Value("${app.whatsapp.group.link:https://chat.whatsapp.com/}")
    private String whatsappGroupLink;
    
    private final ConcurrentHashMap<Long, Boolean> approvedUsers = new ConcurrentHashMap<>();
    
    public String requestAccess(Long userId, String userEmail, String fullName) {
        approvedUsers.putIfAbsent(userId, false);
        
        if (isApproved(userId)) {
            return whatsappGroupLink;
        }
        
        System.out.println("📱 WhatsApp access requested by: " + fullName + " (" + userEmail + ") - User ID: " + userId);
        System.out.println("To approve, call: whatsAppService.approveUser(" + userId + ")");
        
        return null;
    }
    
    public void approveUser(Long userId) {
        approvedUsers.put(userId, true);
        System.out.println("✅ User " + userId + " approved for WhatsApp group");
    }
    
    public boolean isApproved(Long userId) {
        return approvedUsers.getOrDefault(userId, false);
    }
    
    public String getGroupLink(Long userId) {
        return isApproved(userId) ? whatsappGroupLink : null;
    }
    
    public void removeAccess(Long userId) {
        approvedUsers.remove(userId);
        System.out.println("❌ User " + userId + " removed from WhatsApp group");
    }
    
    public ConcurrentHashMap<Long, Boolean> getAllPendingRequests() {
        ConcurrentHashMap<Long, Boolean> pending = new ConcurrentHashMap<>();
        approvedUsers.forEach((id, approved) -> {
            if (!approved) pending.put(id, false);
        });
        return pending;
    }
}