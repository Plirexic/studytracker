package de.hsesslingen.studybackend.model.task;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import de.hsesslingen.studybackend.model.Student;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "mafeit03_task_logs")
@Data
public class TaskLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private TaskAction action;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @CreationTimestamp
    @Column(name = "log_timestamp", updatable = false)
    private LocalDateTime logTimestamp;
}
