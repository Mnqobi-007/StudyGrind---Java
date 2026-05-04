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
        Object requestUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        int statusCode = 500;
        String errorMessage = "Something went wrong";
        String errorTitle = "Something Went Wrong";

        if (status != null) {
            statusCode = Integer.parseInt(status.toString());

            switch (statusCode) {
                case 400:
                    errorTitle = "Bad Request";
                    errorMessage = "The server could not understand your request. Please check your input and try again.";
                    break;
                case 401:
                    errorTitle = "Unauthorized Access";
                    errorMessage = "Please log in to access this page. Your session may have expired.";
                    break;
                case 403:
                    errorTitle = "Access Denied";
                    errorMessage = "You don't have permission to access this page. Please contact an administrator if you believe this is an error.";
                    break;
                case 404:
                    errorTitle = "Page Not Found";
                    errorMessage = "The page you're looking for doesn't exist or has been moved.";
                    break;
                case 500:
                    errorTitle = "Server Error";
                    errorMessage = "Something went wrong on our end. Please try again later or contact support.";
                    break;
                case 503:
                    errorTitle = "Service Unavailable";
                    errorMessage = "The service is temporarily unavailable. Please try again in a few minutes.";
                    break;
                default:
                    errorTitle = "Error " + statusCode;
                    errorMessage = message != null ? message.toString() : "An unexpected error occurred";
            }
        }

        model.addAttribute("statusCode", statusCode);
        model.addAttribute("errorTitle", errorTitle);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("errorPath", requestUri != null ? requestUri.toString() : "");

        return "error";
    }
}