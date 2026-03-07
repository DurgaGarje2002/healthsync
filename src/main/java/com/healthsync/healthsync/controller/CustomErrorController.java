package com.healthsync.healthsync.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        // ✅ Fixed: Jakarta uses "jakarta.servlet.error.status_code"
        Object status = request.getAttribute("jakarta.servlet.error.status_code");

        // Fallback for older containers
        if (status == null) {
            status = request.getAttribute("javax.servlet.error.status_code");
        }

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            return switch (statusCode) {
                case 403 -> "error-403";
                case 404 -> "error-404";
                case 500 -> "error-500";
                default  -> "error-500";
            };
        }

        return "error-500";
    }
}