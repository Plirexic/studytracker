package de.hsesslingen.studybackend.model.task;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;

import de.hsesslingen.studybackend.model.Student;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "mafeit03_tasks")
@Data
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Title cannot be null")
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Due date cannot be null")
    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY) // Many tasks can belong to one student
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
}