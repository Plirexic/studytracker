package de.hsesslingen.studybackend.controller;

import java.util.List;
import java.util.Objects;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import de.hsesslingen.studybackend.repository.StudentRepository;
import de.hsesslingen.studybackend.repository.TaskRepository;
import de.hsesslingen.studybackend.model.task.Task;
import de.hsesslingen.studybackend.model.task.TaskAction;
import de.hsesslingen.studybackend.model.Student;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private StudentRepository studentRepository;

    // POST: Create a new task for a student
    @PostMapping("/students/{studentId}/tasks")
    public ResponseEntity<Task> createTask(@PathVariable Long studentId, @RequestBody Task taskRequest) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found with id " + studentId));
        
        taskRequest.setStudent(student);
        Task createdTask = taskRepository.save(taskRequest);

        // Log task creation using the stored procedure
        taskRepository.logTaskAction(createdTask.getId(), studentId, TaskAction.CREATED.name(), "Task created" + createdTask.getTitle());

        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    // GET: Get all tasks for a student
    @GetMapping("/students/{studentId}/tasks")
    public List<Task> getTasksByStudentId(@PathVariable Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found with id " + studentId);
        }
        return taskRepository.findByStudentId(studentId);
    }

    // GET: Get a specific task by ID
    @GetMapping("/{taskId}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with id " + taskId));
        return new ResponseEntity<>(task, HttpStatus.OK);
    }

    // PUT: Update a task
    @PutMapping("/update/{taskId}")
    public ResponseEntity<Task> updateTask(@PathVariable Long taskId, @RequestBody Task taskDetails) {
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with id " + taskId));
        
        String oldTitle = existingTask.getTitle();
        String oldDescription = existingTask.getDescription();
        LocalDate oldDueDate = existingTask.getDueDate();
        boolean oldCompletionStatus = existingTask.isCompleted();
        
        // Only update fields if they are present in the request body
        if (taskDetails.getTitle() != null) {
            existingTask.setTitle(taskDetails.getTitle());
        }
        if (taskDetails.getDescription() != null) {
            existingTask.setDescription(taskDetails.getDescription());
        }
        if (taskDetails.getDueDate() != null) {
            existingTask.setDueDate(taskDetails.getDueDate());
        }

        // isCompleted always has a value
        if (taskDetails.isCompleted() != oldCompletionStatus) {
            existingTask.setCompleted(taskDetails.isCompleted());
        }

        Task updatedTask = taskRepository.save(existingTask);

        // Log changes
        StringBuilder logDetails = new StringBuilder();

        if (taskDetails.getTitle() != null && !Objects.equals(oldTitle, updatedTask.getTitle())) {
            logDetails.append("Title changed from '").append(oldTitle == null ? "N/A" : oldTitle)
                    .append("' to '").append(updatedTask.getTitle() == null ? "N/A" : updatedTask.getTitle()).append("'. ");
        }

        if (taskDetails.getDescription() != null && !Objects.equals(oldDescription, updatedTask.getDescription())) {
            logDetails.append("Description changed. Old: '").append(oldDescription == null ? "N/A" : oldDescription)
                    .append("', New: '").append(updatedTask.getDescription() == null ? "N/A" : updatedTask.getDescription()).append("'. ");
        }

        if (taskDetails.getDueDate() != null && !Objects.equals(oldDueDate, updatedTask.getDueDate())) {
            logDetails.append("Due date changed. Old: '").append(oldDueDate == null ? "N/A" : oldDueDate)
                    .append("', New: '").append(updatedTask.getDueDate() == null ? "N/A" : updatedTask.getDueDate()).append("'. ");
        }

        if (!Objects.equals(oldCompletionStatus, updatedTask.isCompleted())){
            boolean oldStatusDisplay = oldCompletionStatus;
            boolean newStatusDisplay = updatedTask.isCompleted();
            logDetails.append("Completion status changed from '").append(oldStatusDisplay ? "COMPLETED" : "PENDING")
                    .append("' to '").append(newStatusDisplay ? "COMPLETED" : "PENDING").append("'. ");
        }

        String logString = logDetails.toString().trim();
        if (!logString.isEmpty()) {
            taskRepository.logTaskAction(updatedTask.getId(), updatedTask.getStudent().getId(), TaskAction.UPDATED.name(), logString);
        }

        return ResponseEntity.ok(updatedTask);
    }

    // DELETE: Delete a task
    @DeleteMapping("/delete/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with id " + taskId));
        
        taskRepository.delete(task);

        // Log task deletion using the stored procedure
        taskRepository.logTaskAction(task.getId(), task.getStudent().getId(), TaskAction.DELETED.name(), "Task deleted");

        return ResponseEntity.noContent().build();
    }

    // Endpoint to get the count of pending tasks for a student
    @GetMapping("/students/{studentId}/tasks/pending-count")
    public ResponseEntity<Integer> getPendingTaskCount(@PathVariable Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found with id " + studentId);
        }
        Integer pendingCount = taskRepository.getStudentPendingTaskCount(studentId);
        return new ResponseEntity<>(pendingCount, HttpStatus.OK);
    }
}
