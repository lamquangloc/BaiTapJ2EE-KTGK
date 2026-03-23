package com.example.lamquanglocKTGK.service;

import com.example.lamquanglocKTGK.model.Role;
import com.example.lamquanglocKTGK.model.RoleName;
import com.example.lamquanglocKTGK.model.Student;
import com.example.lamquanglocKTGK.repository.RoleRepository;
import com.example.lamquanglocKTGK.repository.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final StudentRepository studentRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(StudentRepository studentRepository,
                                   RoleRepository roleRepository,
                                   PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = new DefaultOAuth2UserService().loadUser(userRequest);

        String email = oauthUser.getAttribute("email");
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("Khong lay duoc email tu Google");
        }

        Student student = studentRepository.findByEmail(email.toLowerCase())
            .orElseGet(() -> createStudentFromOAuth(oauthUser));

        Set<GrantedAuthority> authorities = student.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
            .collect(Collectors.toSet());

        return new DefaultOAuth2User(authorities, oauthUser.getAttributes(), "email");
    }

    private Student createStudentFromOAuth(OAuth2User oauthUser) {
        Role studentRole = roleRepository.findByName(RoleName.STUDENT)
            .orElseThrow(() -> new EntityNotFoundException("Role STUDENT khong ton tai"));

        String email = oauthUser.getAttribute("email");
        String preferredName = oauthUser.getAttribute("name");
        String baseUsername = preferredName != null && !preferredName.isBlank()
            ? preferredName.trim().replaceAll("\\s+", "").toLowerCase()
            : email.substring(0, email.indexOf('@')).toLowerCase();
        String username = makeUniqueUsername(baseUsername);

        Student student = new Student();
        student.setUsername(username);
        student.setEmail(email.toLowerCase());
        student.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        student.setRoles(new HashSet<>());
        student.getRoles().add(studentRole);

        return studentRepository.save(student);
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
