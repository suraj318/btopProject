package com.btop.service.impl;

import com.btop.dto.TaskDTO;
import com.btop.entity.Task;
import com.btop.entity.User;
import com.btop.exception.DuplicateResourceException;
import com.btop.exception.ResourceNotFoundException;
import com.btop.repository.TaskRepository;
import com.btop.repository.UserRepository;
import com.btop.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepo;
    private final UserRepository userRepo;

    @Override
    public TaskDTO create(TaskDTO dto) {

        log.info("Creating task: {}", dto.getTitle());

        if (taskRepo.existsByTitle(dto.getTitle())) {
            throw new DuplicateResourceException("Task title already exists");
        }

        User user = userRepo.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Task task = Task.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(dto.getStatus())
                .user(user)
                .build();

        return map(taskRepo.save(task));
    }

    @Override
    public TaskDTO get(Long id) {
        return map(taskRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found")));
    }

    @Override
    public Page<TaskDTO> getAll(Pageable pageable) {
        return taskRepo.findAll(pageable).map(this::map);
    }

    @Override
    public TaskDTO update(Long id, TaskDTO dto) {

        Task task = taskRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (!task.getTitle().equals(dto.getTitle()) &&
                taskRepo.existsByTitle(dto.getTitle())) {
            throw new DuplicateResourceException("Task title already exists");
        }

        User user = userRepo.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus());
        task.setUser(user);

        return map(taskRepo.save(task));
    }

    @Override
    public void delete(Long id) {
        taskRepo.deleteById(id);
    }

    private TaskDTO map(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setUserId(task.getUser().getId());
        return dto;
    }

    @Override
    public Page<TaskDTO> getTasksByUser(Long userId, Pageable pageable) {

        // Validate user exists
        if (!userRepo.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        return taskRepo.findByUserId(userId, pageable)
                .map(this::map);
    }

    @Override
    public List<TaskDTO> createBulk(List<TaskDTO> dtos) {

        List<Task> tasks = dtos.stream().map(dto -> {

            //Duplicate check
            if (taskRepo.existsByTitle(dto.getTitle())) {
                throw new DuplicateResourceException("Task title already exists: " + dto.getTitle());
            }

            User user = userRepo.findById(dto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            return Task.builder()
                    .title(dto.getTitle())
                    .description(dto.getDescription())
                    .status(dto.getStatus())
                    .user(user)
                    .build();

        }).toList();

        List<Task> savedTasks = taskRepo.saveAll(tasks);

        return savedTasks.stream()
                .map(this::map)
                .toList();
    }
}
