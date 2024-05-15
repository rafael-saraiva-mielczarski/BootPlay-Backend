package br.com.sysmap.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import br.com.sysmap.domain.entities.Users;
import br.com.sysmap.domain.repository.UsersRepository;

public class UsersServiceTest {

    @Mock
    private UsersRepository usersRepository;

    @InjectMocks
    private UsersService usersService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should return user not found when loading by username")
    void shouldReturnUserNotFound() throws UsernameNotFoundException {
        when(usersRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            usersService.loadUserByUsername("nonexistent@example.com");
        });
    }

    @Test
    @DisplayName("Should return user found when finding by email")
    void shouldReturnUserFound() throws UsernameNotFoundException {
        Users user = mock(Users.class);
        user.setEmail("user@example.com");

        when(usersRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        Users foundUser = usersService.findByEmail("user@example.com");

        assertNotNull(foundUser);
        assertEquals(user.getEmail(), foundUser.getEmail());
    }

    @Test
    @DisplayName("Should return user not found when finding by email")
    void shouldReturnUserNotFoundWhenFindingByEmail() throws UsernameNotFoundException {
        when(usersRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        Users foundUser = usersService.findByEmail("nonexistent@example.com");

        assertNull(foundUser);
    }
}
