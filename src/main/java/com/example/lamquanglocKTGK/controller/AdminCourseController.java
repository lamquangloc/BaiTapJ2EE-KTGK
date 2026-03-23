package com.example.lamquanglocKTGK.controller;

import com.example.lamquanglocKTGK.model.Course;
import com.example.lamquanglocKTGK.repository.CategoryRepository;
import com.example.lamquanglocKTGK.service.CourseService;
import com.example.lamquanglocKTGK.service.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/admin/courses")
public class AdminCourseController {

    private final CourseService courseService;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;

    public AdminCourseController(CourseService courseService,
                                 CategoryRepository categoryRepository,
                                 FileStorageService fileStorageService) {
        this.courseService = courseService;
        this.categoryRepository = categoryRepository;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("courses", courseService.getAll());
        return "admin/courses";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("course", new Course());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("isEdit", false);
        return "admin/course-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("course") Course course,
                         BindingResult bindingResult,
                         @RequestParam(name = "imageFile", required = false) MultipartFile imageFile,
                         Model model) {
        // Validate categoryId
        if (course.getCategoryId() == null) {
            bindingResult.rejectValue("categoryId", "error.categoryId", "Vui long chon danh muc");
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("isEdit", false);
            return "admin/course-form";
        }

        // Resolve categoryId to Category object
        if (course.getCategoryId() != null) {
            course.setCategory(categoryRepository.findById(course.getCategoryId()).orElse(null));
            if (course.getCategory() == null) {
                bindingResult.rejectValue("categoryId", "error.categoryId", "Danh muc khong ton tai");
                model.addAttribute("categories", categoryRepository.findAll());
                model.addAttribute("isEdit", false);
                return "admin/course-form";
            }
        }

        String imagePath = fileStorageService.storeImage(imageFile);
        if (imagePath != null) {
            course.setImage(imagePath);
        }

        courseService.create(course);
        return "redirect:/admin/courses?created";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Course course = courseService.getById(id);
        course.setCategoryId(course.getCategory() != null ? course.getCategory().getId() : null);

        model.addAttribute("courseId", id);
        model.addAttribute("course", course);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("isEdit", true);
        return "admin/course-form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("course") Course course,
                         BindingResult bindingResult,
                         @RequestParam(name = "imageFile", required = false) MultipartFile imageFile,
                         Model model) {
        // Validate categoryId
        if (course.getCategoryId() == null) {
            bindingResult.rejectValue("categoryId", "error.categoryId", "Vui long chon danh muc");
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("courseId", id);
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("isEdit", true);
            return "admin/course-form";
        }

        // Resolve categoryId to Category object
        if (course.getCategoryId() != null) {
            course.setCategory(categoryRepository.findById(course.getCategoryId()).orElse(null));
            if (course.getCategory() == null) {
                bindingResult.rejectValue("categoryId", "error.categoryId", "Danh muc khong ton tai");
                model.addAttribute("courseId", id);
                model.addAttribute("categories", categoryRepository.findAll());
                model.addAttribute("isEdit", true);
                return "admin/course-form";
            }
        }

        Course existingCourse = courseService.getById(id);
        String imagePath = fileStorageService.storeImage(imageFile);
        if (imagePath != null) {
            course.setImage(imagePath);
        } else {
            course.setImage(existingCourse.getImage());
        }

        courseService.update(id, course);
        return "redirect:/admin/courses?updated";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        courseService.delete(id);
        return "redirect:/admin/courses?deleted";
    }
}
