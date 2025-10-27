package app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "User Management API is running! Use /api/users endpoints.";
    }

    @GetMapping("/test")
    public String test() {
        return "Application is working correctly!";
    }
}