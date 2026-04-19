package com.btop.service.impl;

import com.btop.dto.UserDTO;
import com.btop.entity.User;
import com.btop.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    // ================= CREATE USER =================

    @Test
    void createUser_success() {

        // GIVEN
        UserDTO dto = new UserDTO();
        dto.setUsername("john");
        dto.setPassword("1234");
        dto.setRole("USER");

        // Mock DB response (BaseEntity ID)
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L); // 🔥 simulate DB-generated ID
            return user;
        });

        // WHEN
        UserDTO result = userService.create(dto);

        // THEN
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("john", result.getUsername());
        assertEquals("USER", result.getRole());

        // Verify repository interaction
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_duplicate_shouldThrow() {

        UserDTO dto = new UserDTO();
        dto.setUsername("john");

        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> userService.create(dto));
    }
}