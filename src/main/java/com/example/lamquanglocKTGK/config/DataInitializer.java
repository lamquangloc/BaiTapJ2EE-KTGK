package com.example.lamquanglocKTGK.config;

import com.example.lamquanglocKTGK.model.Category;
import com.example.lamquanglocKTGK.model.Course;
import com.example.lamquanglocKTGK.model.Role;
import com.example.lamquanglocKTGK.model.RoleName;
import com.example.lamquanglocKTGK.model.Student;
import com.example.lamquanglocKTGK.repository.CategoryRepository;
import com.example.lamquanglocKTGK.repository.CourseRepository;
import com.example.lamquanglocKTGK.repository.RoleRepository;
import com.example.lamquanglocKTGK.repository.StudentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner seedData(RoleRepository roleRepository,
                                      StudentRepository studentRepository,
                                      CategoryRepository categoryRepository,
                                      CourseRepository courseRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ADMIN)));
            Role studentRole = roleRepository.findByName(RoleName.STUDENT)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.STUDENT)));

            if (!studentRepository.existsByUsername("admin")) {
                Student admin = new Student();
                admin.setUsername("admin");
                admin.setEmail("admin@example.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRoles(Set.of(adminRole));
                studentRepository.save(admin);
            }

            if (!studentRepository.existsByUsername("student")) {
                Student student = new Student();
                student.setUsername("student");
                student.setEmail("student@example.com");
                student.setPassword(passwordEncoder.encode("student123"));
                student.setRoles(Set.of(studentRole));
                studentRepository.save(student);
            }

            if (categoryRepository.count() == 0) {
                categoryRepository.saveAll(List.of(
                    new Category("Cong nghe thong tin"),
                    new Category("Kinh te"),
                    new Category("Ngoai ngu")
                ));
            }

            if (courseRepository.count() == 0) {
                List<Category> categories = categoryRepository.findAll();
                Category it = categories.get(0);
                Category biz = categories.get(1);
                Category lang = categories.get(2);

                courseRepository.saveAll(List.of(
                    createCourse("Lap trinh Java", 3, "ThS. Nguyen Van A", it, "https://images.unsplash.com/photo-1515879218367-8466d910aaa4"),
                    createCourse("Cau truc du lieu", 3, "ThS. Tran Thi B", it, "https://images.unsplash.com/photo-1461749280684-dccba630e2f6"),
                    createCourse("Co so du lieu", 3, "TS. Le Van C", it, "https://images.unsplash.com/photo-1558494949-ef010cbdcc31"),
                    createCourse("Mang may tinh", 2, "ThS. Pham Van D", it, "https://images.unsplash.com/photo-1518770660439-4636190af475"),
                    createCourse("Marketing can ban", 2, "ThS. Hoang Thi E", biz, "https://images.unsplash.com/photo-1460925895917-afdab827c52f"),
                    createCourse("Quan tri hoc", 2, "TS. Ngo Van F", biz, "https://images.unsplash.com/photo-1454165804606-c3d57bc86b40"),
                    createCourse("Tieng Anh giao tiep", 3, "Co. Do Thi G", lang, "https://images.unsplash.com/photo-1521587760476-6c12a4b040da"),
                    createCourse("Tieng Nhat co ban", 3, "Co. Nguyen Thi H", lang, "https://images.unsplash.com/photo-1529078155058-5d716f45d604")
                ));
            }
        };
    }

    private Course createCourse(String name, int credits, String lecturer, Category category, String image) {
        Course course = new Course();
        course.setName(name);
        course.setCredits(credits);
        course.setLecturer(lecturer);
        course.setCategoryId(category.getId());
        course.setCategory(category);
        course.setImage(image);
        return course;
    }
}
