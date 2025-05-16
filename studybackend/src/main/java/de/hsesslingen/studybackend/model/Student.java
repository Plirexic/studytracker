package de.hsesslingen.studybackend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "mafeit03_students")
@Data
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;
}

