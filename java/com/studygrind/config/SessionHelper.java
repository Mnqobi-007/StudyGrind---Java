package com.studygrind.config;

import com.studygrind.model.User;
import com.studygrind.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SessionHelper {

    private UserRepository userRepository;

    public SessionHelper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Long getCurrentUserId() {
        User user = getCurrentUser();
        if (user == null) {
            return null;
        }
        return user.getId();
    }

    public User getCurrentUser() {
        String email = getCurrentUserEmail();
        if (email == null) {
            return null;
        }
        return userRepository.findByEmail(email).orElse(null);
    }

    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        return authentication.getName();
    }
}
