package com.example.lamquanglocKTGK.controller;

import com.example.lamquanglocKTGK.service.EnrollmentService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping("/enroll/{courseId}")
    public String enroll(@PathVariable Long courseId, Authentication authentication) {
        boolean enrolled = enrollmentService.enroll(resolvePrincipalIdentifier(authentication), courseId);
        if (enrolled) {
            return "redirect:/home?enrollSuccess";
        }
        return "redirect:/home?alreadyEnrolled";
    }

    @GetMapping("/enroll/my-courses")
    public String myCourses(Authentication authentication, Model model) {
        model.addAttribute("enrollments", enrollmentService.getMyEnrollments(resolvePrincipalIdentifier(authentication)));
        return "my-courses";
    }

    private String resolvePrincipalIdentifier(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            if (email != null && !email.isBlank()) {
                return email;
            }
        }
        return authentication.getName();
    }
}
