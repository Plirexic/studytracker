package de.hsesslingen.studybackend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

import de.hsesslingen.studybackend.model.task.Task;

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

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY) // One student can have many tasks
    private List<Task> tasks = new ArrayList<>();
}

