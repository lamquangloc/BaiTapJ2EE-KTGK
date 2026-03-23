package com.example.lamquanglocKTGK.service;

import com.example.lamquanglocKTGK.model.Role;
import com.example.lamquanglocKTGK.model.RoleName;
import com.example.lamquanglocKTGK.model.Student;
import com.example.lamquanglocKTGK.repository.RoleRepository;
import com.example.lamquanglocKTGK.repository.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository studentRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(Student request) {
        if (studentRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username da ton tai");
        }
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email da ton tai");
        }

        Role studentRole = roleRepository.findByName(RoleName.STUDENT)
            .orElseThrow(() -> new EntityNotFoundException("Role STUDENT khong ton tai"));

        Student student = new Student();
        student.setUsername(request.getUsername().trim());
        student.setPassword(passwordEncoder.encode(request.getPassword()));
        student.setEmail(request.getEmail().trim().toLowerCase());
        student.getRoles().add(studentRole);

        studentRepository.save(student);
    }

    @Transactional(readOnly = true)
    public Student getByUsername(String username) {
        return studentRepository.findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException("Khong tim thay sinh vien: " + username));
    }

    @Transactional(readOnly = true)
    public Student getByUsernameOrEmail(String principalName) {
        return studentRepository.findByUsername(principalName)
            .or(() -> studentRepository.findByEmail(principalName.toLowerCase()))
            .orElseThrow(() -> new EntityNotFoundException("Khong tim thay sinh vien: " + principalName));
    }

    @Transactional
    public Student ensureOAuth2Student(String email, String preferredName) {
        String normalizedEmail = email.trim().toLowerCase();
        return studentRepository.findByEmail(normalizedEmail)
            .orElseGet(() -> {
                Role studentRole = roleRepository.findByName(RoleName.STUDENT)
                    .orElseThrow(() -> new EntityNotFoundException("Role STUDENT khong ton tai"));

                String baseUsername = preferredName != null && !preferredName.isBlank()
                    ? preferredName.trim().replaceAll("\\s+", "").toLowerCase()
                    : normalizedEmail.substring(0, normalizedEmail.indexOf('@'));

                Student student = new Student();
                student.setUsername(makeUniqueUsername(baseUsername));
                student.setEmail(normalizedEmail);
                student.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                student.getRoles().add(studentRole);
                return studentRepository.save(student);
            });
    }

    private String makeUniqueUsername(String baseUsername) {
        String username = baseUsername;
        int suffix = 1;
        while (studentRepository.existsByUsername(username)) {
            username = baseUsername + suffix;
            suffix++;
        }
        return username;
    }
}
