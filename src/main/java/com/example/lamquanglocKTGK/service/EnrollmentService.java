package com.example.lamquanglocKTGK.service;

import com.example.lamquanglocKTGK.model.Course;
import com.example.lamquanglocKTGK.model.Enrollment;
import com.example.lamquanglocKTGK.model.Student;
import com.example.lamquanglocKTGK.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentService studentService;
    private final CourseService courseService;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             StudentService studentService,
                             CourseService courseService) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentService = studentService;
        this.courseService = courseService;
    }

    @Transactional
    public boolean enroll(String principalName, Long courseId) {
        Student student = studentService.getByUsernameOrEmail(principalName);
        Course course = courseService.getById(courseId);

        if (enrollmentRepository.existsByStudentAndCourse(student, course)) {
            return false;
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrollDate(LocalDate.now());
        enrollmentRepository.save(enrollment);
        return true;
    }

    @Transactional(readOnly = true)
    public boolean isEnrolled(String principalName, Long courseId) {
        Student student = studentService.getByUsernameOrEmail(principalName);
        Course course = courseService.getById(courseId);
        return enrollmentRepository.existsByStudentAndCourse(student, course);
    }

    @Transactional(readOnly = true)
    public List<Enrollment> getMyEnrollments(String principalName) {
        Student student = studentService.getByUsernameOrEmail(principalName);
        return enrollmentRepository.findByStudent(student);
    }
}
