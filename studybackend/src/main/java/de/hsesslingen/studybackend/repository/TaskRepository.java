package de.hsesslingen.studybackend.repository;

import de.hsesslingen.studybackend.model.task.Task;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStudentId(Long studentId);

    // Calling the stored procedure to log task actions
    @Procedure(procedureName = "mafeit03_logTaskAction")
    void logTaskAction(@Param("p_task_id") Long taskId, 
                       @Param("p_student_id") Long studentId, 
                       @Param("p_action") String action, 
                       @Param("p_details") String details);

    // Calling the database function to get the count of pending tasks for a student
    @Query(value = "SELECT dbo.mafeit03_getStudentPendingTaskCount(:studentId)", nativeQuery = true)
    Integer getStudentPendingTaskCount(@Param("studentId") Long studentId); 
}
