package com.studygrind.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class PageController {
    private com.studygrind.config.SessionHelper sessionHelper;

    @org.springframework.beans.factory.annotation.Autowired
    public PageController(com.studygrind.config.SessionHelper sessionHelper) {
        this.sessionHelper = sessionHelper;
    }

    @GetMapping("/")
    public String index(HttpSession session) {
        String role = (String) session.getAttribute("userRole");
        if (role != null) {
            if ("admin".equals(role)) return "redirect:/admin/dashboard";
            if ("teacher".equals(role)) return "redirect:/teacher/dashboard";
            return "redirect:/student/dashboard";
        }
        return "login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/student/dashboard")
    public String studentDashboard(HttpSession session, org.springframework.ui.Model model) {
        String role = (String) session.getAttribute("userRole");
        if (!"student".equals(role)) return "redirect:/";
        model.addAttribute("user", session.getAttribute("userFullName"));
        model.addAttribute("fullName", session.getAttribute("userFullName"));
        model.addAttribute("email", session.getAttribute("userEmail"));
        model.addAttribute("username", session.getAttribute("userName"));
        return "student_dashboard";
    }

    @GetMapping("/teacher/dashboard")
    public String teacherDashboard(HttpSession session, org.springframework.ui.Model model) {
        String role = (String) session.getAttribute("userRole");
        if (!"teacher".equals(role)) return "redirect:/";
        model.addAttribute("fullName", session.getAttribute("userFullName"));
        model.addAttribute("email", session.getAttribute("userEmail"));
        model.addAttribute("username", session.getAttribute("userName"));
        return "teacher_dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session, org.springframework.ui.Model model) {
        String role = (String) session.getAttribute("userRole");
        if (!"admin".equals(role)) return "redirect:/";
        model.addAttribute("fullName", session.getAttribute("userFullName"));
        model.addAttribute("email", session.getAttribute("userEmail"));
        model.addAttribute("username", session.getAttribute("userName"));
        return "admin_dashboard";
    }

    @GetMapping("/health")
    @ResponseBody
    public Map<String, Object> health() {
        return Map.of("status", "healthy");
    }
}
