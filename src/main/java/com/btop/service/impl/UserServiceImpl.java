package com.btop.service.impl;

import com.btop.dto.UserDTO;
import com.btop.entity.User;
import com.btop.repository.UserRepository;
import com.btop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDTO create(UserDTO dto) {

        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .password(dto.getPassword())
                .role(dto.getRole())
                .build();

        User saved = userRepository.save(user);

        dto.setId(saved.getId());
        return dto;
    }
}
