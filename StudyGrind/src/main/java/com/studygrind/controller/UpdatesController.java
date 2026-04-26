package com.studygrind.controller;

import com.studygrind.security.UserPrincipal;
import com.studygrind.service.UpdatesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/updates")
public class UpdatesController {
    
    @Autowired
    private UpdatesService updatesService;
    
    @GetMapping
    public ResponseEntity<?> checkForUpdates(@RequestParam Long since,
                                              @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean hasUpdates = updatesService.hasUpdatesSince(since, currentUser.getId());
        Map<String, Object> response = new HashMap<>();
        response.put("hasUpdates", hasUpdates);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}