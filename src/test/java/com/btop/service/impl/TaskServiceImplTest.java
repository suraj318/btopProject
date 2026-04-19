package com.btop.service.impl;

import com.btop.dto.TaskDTO;
import com.btop.entity.Task;
import com.btop.entity.User;
import com.btop.enums.TaskStatus;
import com.btop.exception.DuplicateResourceException;
import com.btop.exception.ResourceNotFoundException;
import com.btop.repository.TaskRepository;
import com.btop.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepo;

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private TaskServiceImpl service;

    // ================= CREATE =================

    @Test
    void createTask_success() {
        TaskDTO dto = new TaskDTO();
        dto.setTitle("Task1");
        dto.setDescription("Demo");
        dto.setStatus(TaskStatus.TODO);
        dto.setUserId(1L);

        User user = User.builder().build();
        user.setId(1L); // 🔥 BaseEntity field

        when(taskRepo.existsByTitle("Task1")).thenReturn(false);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        when(taskRepo.save(any(Task.class))).thenAnswer(invocation -> {
            Task saved = invocation.getArgument(0);
            saved.setId(100L); // 🔥 BaseEntity ID
            return saved;
        });

        TaskDTO result = service.create(dto);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("Task1", result.getTitle());
        assertEquals(1L, result.getUserId());
    }

    @Test
    void createTask_duplicateTitle_shouldThrow() {
        TaskDTO dto = new TaskDTO();
        dto.setTitle("Task1");

        when(taskRepo.existsByTitle("Task1")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> service.create(dto));
    }

    @Test
    void createTask_userNotFound_shouldThrow() {
        TaskDTO dto = new TaskDTO();
        dto.setTitle("Task1");
        dto.setUserId(1L);

        when(taskRepo.existsByTitle("Task1")).thenReturn(false);
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.create(dto));
    }

    // ================= GET =================

    @Test
    void getTask_success() {
        User user = new User();
        user.setId(1L);

        Task task = Task.builder()
                .title("Task1")
                .description("Demo")
                .status(TaskStatus.TODO)
                .user(user)
                .build();

        task.setId(10L); // 🔥 BaseEntity ID

        when(taskRepo.findById(10L)).thenReturn(Optional.of(task));

        TaskDTO result = service.get(10L);

        assertEquals(10L, result.getId());
        assertEquals("Task1", result.getTitle());
        assertEquals(1L, result.getUserId());
    }

    @Test
    void getTask_notFound_shouldThrow() {
        when(taskRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.get(1L));
    }

    // ================= UPDATE =================

    @Test
    void updateTask_success() {
        User user = new User();
        user.setId(1L);

        Task existing = Task.builder()
                .title("Old")
                .user(user)
                .build();
        existing.setId(1L);

        TaskDTO dto = new TaskDTO();
        dto.setTitle("New");
        dto.setDescription("Updated");
        dto.setStatus(TaskStatus.IN_PROGRESS);
        dto.setUserId(1L);

        when(taskRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(taskRepo.existsByTitle("New")).thenReturn(false);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(taskRepo.save(any(Task.class))).thenReturn(existing);

        TaskDTO result = service.update(1L, dto);

        assertEquals("New", result.getTitle());
    }

    @Test
    void updateTask_duplicateTitle_shouldThrow() {
        Task existing = new Task();
        existing.setTitle("Old");

        when(taskRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(taskRepo.existsByTitle("New")).thenReturn(true);

        TaskDTO dto = new TaskDTO();
        dto.setTitle("New");

        assertThrows(DuplicateResourceException.class,
                () -> service.update(1L, dto));
    }

    // ================= DELETE =================

    @Test
    void deleteTask_success() {
        service.delete(1L);
        verify(taskRepo).deleteById(1L);
    }

    // ================= PAGINATION =================

    @Test
    void getAllTasks_withPagination_success() {
        User user = new User();
        user.setId(1L);

        Task task = Task.builder()
                .title("Task1")
                .user(user)
                .build();
        task.setId(1L);

        Page<Task> page = new PageImpl<>(java.util.List.of(task));

        when(taskRepo.findAll(any(Pageable.class))).thenReturn(page);

        Page<TaskDTO> result = service.getAll(PageRequest.of(0, 5));

        assertEquals(1, result.getTotalElements());
    }

    // ================= GET BY USER =================

    @Test
    void getTasksByUser_success() {
        Long userId = 1L;

        when(userRepo.existsById(userId)).thenReturn(true);

        User user = new User();
        user.setId(userId);

        Task task = Task.builder()
                .title("Task1")
                .user(user)
                .build();
        task.setId(1L);

        Page<Task> page = new PageImpl<>(java.util.List.of(task));

        when(taskRepo.findByUserId(eq(userId), any(Pageable.class)))
                .thenReturn(page);

        Page<TaskDTO> result = service.getTasksByUser(userId, PageRequest.of(0, 5));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getTasksByUser_userNotFound_shouldThrow() {
        when(userRepo.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> service.getTasksByUser(1L, PageRequest.of(0, 5)));
    }

    @Test
    void createBulk_success() {

        TaskDTO dto1 = new TaskDTO();
        dto1.setTitle("Task1");
        dto1.setDescription("Desc1");
        dto1.setStatus(TaskStatus.TODO);
        dto1.setUserId(1L);

        TaskDTO dto2 = new TaskDTO();
        dto2.setTitle("Task2");
        dto2.setDescription("Desc2");
        dto2.setStatus(TaskStatus.IN_PROGRESS);
        dto2.setUserId(1L);

        User user = new User();
        user.setId(1L);

        when(taskRepo.existsByTitle(anyString())).thenReturn(false);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        when(taskRepo.saveAll(anyList())).thenAnswer(invocation -> {
            List<Task> tasks = invocation.getArgument(0);
            tasks.forEach(t -> t.setId(100L));
            return tasks;
        });

        List<TaskDTO> result = service.createBulk(List.of(dto1, dto2));

        assertEquals(2, result.size());
        verify(taskRepo).saveAll(anyList());
    }
}