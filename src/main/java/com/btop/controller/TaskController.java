package com.btop.controller;

import com.btop.dto.TaskDTO;
import com.btop.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Slf4j
//@Tag(name = "Task API", description = "Task Management APIs")
public class TaskController {

    private final TaskService taskService;

    // ================= CREATE =================
    @PostMapping
   // @Operation(summary = "Create a new task")
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody TaskDTO dto) {

        log.info("Create Task request: title={}, userId={}", dto.getTitle(), dto.getUserId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(taskService.create(dto));
    }

    // ================= GET BY ID =================
    @GetMapping("/{id}")
   // @Operation(summary = "Get task by ID")
    public ResponseEntity<TaskDTO> getTask(@PathVariable Long id) {

        log.info("Get Task request: id={}", id);

        return ResponseEntity.ok(taskService.get(id));
    }

    // ================= GET ALL (PAGINATION) =================
    @GetMapping
    //@Operation(summary = "Get all tasks with pagination")
    public ResponseEntity<Page<TaskDTO>> getAllTasks(Pageable pageable) {

        log.info("REST request to get all tasks");

        Page<TaskDTO> tasks = taskService.getAll(pageable);

        return ResponseEntity.ok(tasks);
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    //@Operation(summary = "Update task")
    public ResponseEntity<TaskDTO> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskDTO dto) {

        log.info("REST request to update Task id: {}", id);

        TaskDTO updatedTask = taskService.update(id, dto);

        return ResponseEntity.ok(updatedTask);
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    //@Operation(summary = "Delete task")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {

        log.info("REST request to delete Task id: {}", id);

        taskService.delete(id);

        return ResponseEntity.noContent().build();
    }

    // ================= GET TASKS BY USER =================
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<TaskDTO>> getTasksByUser(
            @PathVariable Long userId,
            Pageable pageable) {

        log.info("Get Tasks by User request: userId={}", userId);

        Page<TaskDTO> tasks = taskService.getTasksByUser(userId, pageable);

        return ResponseEntity.ok(tasks);
    }

    // Bulk
    @PostMapping("/bulk")
    public ResponseEntity<List<TaskDTO>> createBulkTasks(
            @Valid @RequestBody List<TaskDTO> dtos) {

        log.info("Bulk create request: size={}", dtos.size());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createBulk(dtos));
    }
}