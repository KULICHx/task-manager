package org.example.taskmanager.service;

import org.example.taskmanager.dto.user.UserRequest;
import org.example.taskmanager.dto.user.UserResponse;
import org.example.taskmanager.entity.User;
import org.example.taskmanager.exception.ResourceNotFoundException;
import org.example.taskmanager.mapper.UserMapper;
import org.example.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream().map(UserMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return UserMapper.toResponse(findEntity(id));
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .build();
        return UserMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    @CacheEvict(value = "tasks", allEntries = true)
    public UserResponse update(Long id, UserRequest request) {
        User user = findEntity(id);
        user.setName(request.name());
        user.setEmail(request.email());
        return UserMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    @CacheEvict(value = "tasks", allEntries = true)
    public void delete(Long id) {
        userRepository.delete(findEntity(id));
    }

    @Transactional(readOnly = true)
    public User findEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}
