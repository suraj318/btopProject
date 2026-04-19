package com.btop.service;

import com.btop.dto.TaskDTO;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {
    TaskDTO create(TaskDTO dto);
    TaskDTO get(Long id);
    Page<TaskDTO> getAll(Pageable pageable);
    TaskDTO update(Long id, TaskDTO dto);
    void delete(Long id);
    Page<TaskDTO> getTasksByUser(Long userId, Pageable pageable);

    List<TaskDTO> createBulk(@Valid List<TaskDTO> dtos);
}