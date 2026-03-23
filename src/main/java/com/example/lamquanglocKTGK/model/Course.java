package com.example.lamquanglocKTGK.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "course")
@Getter
@Setter
@NoArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Ten hoc phan khong duoc de trong")
    @Size(max = 150, message = "Ten hoc phan toi da 150 ky tu")
    @Column(nullable = false, length = 150)
    private String name;

    @Size(max = 255, message = "URL hinh anh toi da 255 ky tu")
    @Column(length = 255)
    private String image;

    @NotNull(message = "So tin chi khong duoc de trong")
    @Min(value = 1, message = "So tin chi phai lon hon 0")
    @Column(nullable = false)
    private Integer credits;

    @NotBlank(message = "Giang vien khong duoc de trong")
    @Size(max = 100, message = "Ten giang vien toi da 100 ky tu")
    @Column(nullable = false, length = 100)
    private String lecturer;

    @Transient
    private Long categoryId;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}
