package com.example.lamquanglocKTGK.controller;

import com.example.lamquanglocKTGK.model.Course;
import com.example.lamquanglocKTGK.model.Enrollment;
import com.example.lamquanglocKTGK.service.CourseService;
import com.example.lamquanglocKTGK.service.EnrollmentService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    public HomeController(CourseService courseService, EnrollmentService enrollmentService) {
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping({"/", "/home", "/courses"})
    public String home(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "") String keyword,
                       Model model,
                       Authentication authentication) {

        Page<Course> coursePage = courseService.getCourses(keyword, page, 5);
        model.addAttribute("coursePage", coursePage);
        model.addAttribute("keyword", keyword);

        boolean loggedIn = authentication != null && authentication.isAuthenticated()
            && !"anonymousUser".equals(authentication.getPrincipal());
        model.addAttribute("loggedIn", loggedIn);

        if (loggedIn) {
            String principalIdentifier = resolvePrincipalIdentifier(authentication);
            Set<Long> enrolledCourseIds = enrollmentService.getMyEnrollments(principalIdentifier).stream()
                .map(Enrollment::getCourse)
                .map(Course::getId)
                .collect(Collectors.toSet());
            model.addAttribute("enrolledCourseIds", enrolledCourseIds);
        }

        return "home";
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
