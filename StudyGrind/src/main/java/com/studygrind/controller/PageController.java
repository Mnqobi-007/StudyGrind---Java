package com.studygrind.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/student/dashboard")
    public String studentDashboard() {
        return "student_dashboard";
    }

    @GetMapping("/teacher/dashboard")
    public String teacherDashboard() {
        return "teacher_dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin_dashboard";
    }

    @GetMapping("/payment")
    public String paymentPage() {
        return "payment";
    }

    @GetMapping("/pay-all")
    public String payAllPage() {
        return "pay-all";
    }

    @GetMapping("/payment/success")
    public String paymentSuccess() {
        return "redirect:/student/dashboard?payment=success";
    }

    @GetMapping("/payment/cancel")
    public String paymentCancel() {
        return "redirect:/student/dashboard?payment=cancel";
    }
}