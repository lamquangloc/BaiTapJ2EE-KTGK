package com.example.lamquanglocKTGK.repository;

import com.example.lamquanglocKTGK.model.Course;
import com.example.lamquanglocKTGK.model.Enrollment;
import com.example.lamquanglocKTGK.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByStudentAndCourse(Student student, Course course);

    List<Enrollment> findByStudent(Student student);
}
