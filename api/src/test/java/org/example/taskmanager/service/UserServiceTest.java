package org.example.taskmanager.service;

import org.example.taskmanager.dto.user.UserRequest;
import org.example.taskmanager.dto.user.UserResponse;
import org.example.taskmanager.entity.User;
import org.example.taskmanager.exception.ResourceNotFoundException;
import org.example.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("getById should return user response when user exists")
    void getById_ShouldReturnUserResponse_WhenUserExists() {
        var user = createUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("test");
        assertThat(response.email()).isEqualTo("test@example.com");
    }

    private User createUser() {
        return User.builder()
                .id(1L)
                .name("test")
                .email("test@example.com")
                .build();
    }

    @Test
    @DisplayName("update should modify user and return updated response")
    void update_ShouldUpdateAndReturnUserResponse() {
        var existingUser = createUser();
        var request = new UserRequest("new name", "new@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        UserResponse response = userService.update(1L, request);

        assertThat(response.name()).isEqualTo("new name");
        assertThat(response.email()).isEqualTo("new@example.com");
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("getById should throw exception when user does not exist")
    void getById_ShouldThrowException_WhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("delete should remove user when user exists")
    void delete_ShouldRemoveUser_WhenUserExists() {
        var existingUser = createUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        userService.delete(1L);

        verify(userRepository).delete(existingUser);
    }

    @Test
    @DisplayName("getAll should return list of all users")
    void getAll_ShouldReturnListOfUsers() {
        var user1 = createUser();
        var user2 = User.builder().id(2L).name("second").email("second@example.com").build();

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserResponse> responses = userService.getAll();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).name()).isEqualTo("test");
        assertThat(responses.get(1).name()).isEqualTo("second");
    }

}