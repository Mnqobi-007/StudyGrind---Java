package com.studygrind.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorPageController implements ErrorController {
    
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        
        int statusCode = 500;
        String errorMessage = "Something went wrong";
        
        if (status != null) {
            statusCode = Integer.parseInt(status.toString());
            
            switch (statusCode) {
                case 400:
                    errorMessage = "Bad Request - The server could not understand your request";
                    break;
                case 401:
                    errorMessage = "Unauthorized - Please login to access this page";
                    break;
                case 403:
                    errorMessage = "Forbidden - You don't have permission to access this page";
                    break;
                case 404:
                    errorMessage = "Page Not Found - The page you're looking for doesn't exist";
                    break;
                case 500:
                    errorMessage = "Internal Server Error - Something went wrong on our end";
                    break;
                default:
                    errorMessage = "An unexpected error occurred";
            }
        }
        
        model.addAttribute("statusCode", statusCode);
        model.addAttribute("errorMessage", errorMessage);
        
        return "error";
    }
}