package de.hsesslingen.studybackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import de.hsesslingen.studybackend.model.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
}

