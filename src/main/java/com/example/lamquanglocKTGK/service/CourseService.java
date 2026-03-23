package com.example.lamquanglocKTGK.service;

import com.example.lamquanglocKTGK.model.Category;
import com.example.lamquanglocKTGK.model.Course;
import com.example.lamquanglocKTGK.repository.CategoryRepository;
import com.example.lamquanglocKTGK.repository.CourseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;

    public CourseService(CourseRepository courseRepository, CategoryRepository categoryRepository) {
        this.courseRepository = courseRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public Page<Course> getCourses(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        if (keyword == null || keyword.isBlank()) {
            return courseRepository.findAll(pageable);
        }
        return courseRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public Course getById(Long id) {
        return courseRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Khong tim thay hoc phan voi id=" + id));
    }

    @Transactional(readOnly = true)
    public List<Course> getAll() {
        return courseRepository.findAll(Sort.by("id").descending());
    }

    @Transactional
    public Course create(Course form) {
        Course course = new Course();
        applyForm(course, form);
        return courseRepository.save(course);
    }

    @Transactional
    public Course update(Long id, Course form) {
        Course course = getById(id);
        applyForm(course, form);
        return courseRepository.save(course);
    }

    @Transactional
    public void delete(Long id) {
        courseRepository.deleteById(id);
    }

    private void applyForm(Course course, Course form) {
        Category category = categoryRepository.findById(form.getCategoryId())
            .orElseThrow(() -> new EntityNotFoundException("Khong tim thay danh muc"));

        course.setName(form.getName());
        course.setImage(form.getImage());
        course.setCredits(form.getCredits());
        course.setLecturer(form.getLecturer());
        course.setCategory(category);
    }
}
