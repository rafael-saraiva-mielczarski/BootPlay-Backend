package br.com.sysmap.bootcamp.domain.service;

import br.com.sysmap.bootcamp.domain.entities.Users;
import br.com.sysmap.bootcamp.domain.repository.UsersRepository;
import br.com.sysmap.bootcamp.domain.repository.WalletRepository;
import br.com.sysmap.bootcamp.dto.AuthDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UsersServicesTest {

    @Autowired
    private UsersService usersService;

    @MockBean
    private UsersRepository usersRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private WalletRepository walletRepository;

    @Test
    @DisplayName("Should return users when valid users is saved")
    public void shouldReturnUsersWhenValidUsersIsSaved() {
        UsersRepository usersRepository = mock(UsersRepository.class);
        UsersService usersService = new UsersService(usersRepository, passwordEncoder, walletRepository);

        Users usersToSave = Users.builder().id(1L).name("teste").email("test").password("teste").build();

        when(usersRepository.save(any(Users.class))).thenReturn(usersToSave);

        Users savedUsers = usersService.createUser(usersToSave);

        assertEquals(usersToSave, savedUsers);
    }


    @Test
    @DisplayName("Should throw RuntimeException when email is already in use")
    public void shouldThrowRuntimeExceptionWhenEmailIsAlreadyInUse() {
        Users existingUser = Users.builder().id(1L).name("existing").email("existing@example.com").password("existing").build();
        Mockito.when(usersRepository.findByEmail(existingUser.getEmail())).thenReturn(Optional.of(existingUser));

        assertThrows(RuntimeException.class, () -> {
            usersService.createUser(existingUser);
        });
    }

    @Test
    @DisplayName("Should throw RuntimeException when an error occurs during user creation")
    public void shouldThrowRuntimeExceptionWhenErrorOccursDuringUserCreation() {
        Users newUser = Users.builder().id(2L).name("new").email("new@example.com").password("new").build();
        Mockito.when(usersRepository.findByEmail(newUser.getEmail())).thenReturn(Optional.empty());
        Mockito.when(usersRepository.save(any(Users.class))).thenThrow(RuntimeException.class); // Mocking save to throw an exception

        assertThrows(RuntimeException.class, () -> {
            usersService.createUser(newUser);
        });
    }

    @Test
    @DisplayName("Should return all users created")
    public void shouldReturnAllUsersCreated() {
        List<Users> userList = new ArrayList<>();
        Users user1 = Users.builder().id(1L).name("teste").email("test").password("teste").build();
        Users user2 = Users.builder().id(1L).name("teste2").email("test2").password("teste2").build();
        userList.add(user1);
        userList.add(user2);

        when(usersRepository.findAll()).thenReturn(userList);
        List<Users> result = usersService.getUsers();

        verify(usersRepository, times(1)).findAll();
        assertEquals(userList, result);
    }

    @Test
    @DisplayName("Should return user when valid id is provided")
    public void shouldReturnUserWhenValidIdIsProvided() {
        Users user = Users.builder().id(1L).name("teste").email("teste").password("teste").build();
        when(usersRepository.findById(1L)).thenReturn(Optional.of(user));

        Users foundUser = usersService.getUserById(1L);
        assertEquals(user, foundUser);
    }

    @Test
    @DisplayName("Should update user when valid user is provided")
    public void shouldUpdateUserWhenValidUserIsProvided() {
        Users existingUser = Users.builder().id(1L).name("existingName").email("existingEmail@example.com").password("existingPassword").build();
        when(usersRepository.findByEmail(existingUser.getEmail())).thenReturn(Optional.of(existingUser));

        Users updatedUser = Users.builder().id(1L).name("updatedName").email("existingEmail@example.com").password("updatedPassword").build();
        when(usersRepository.save(existingUser)).thenReturn(updatedUser);

        Users result = usersService.updateUser(updatedUser);

        assertEquals(updatedUser, result);

        verify(usersRepository, times(1)).save(existingUser);
    }

    @Test
    @DisplayName("Should load user by username")
    public void shouldLoadUserByUsername() {
        String email = "test@example.com";
        String password = "password";

        Users user = Users.builder().id(1L).name("Test User").email(email).password(password).build();

        when(usersRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserDetails userDetails = usersService.loadUserByUsername(email);

        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals(password, userDetails.getPassword());
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user is not found by username")
    public void shouldThrowUsernameNotFoundExceptionWhenUserNotFound() {
        String email = "test@example.com";

        when(usersRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> usersService.loadUserByUsername(email));
    }

    @Test
    @DisplayName("Should find user by email")
    public void shouldFindUserByEmail() {
        String email = "test@example.com";
        Users user = Users.builder().id(1L).name("Test User").email(email).password("password").build();

        when(usersRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Users foundUser = usersService.findByEmail(email);

        assertNotNull(foundUser);
        assertEquals(email, foundUser.getEmail());
    }

    @Test
    @DisplayName("Should throw RuntimeException when user is not found by email")
    public void shouldThrowRuntimeExceptionWhenUserNotFoundByEmail() {
        String email = "test@example.com";

        when(usersRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> usersService.findByEmail(email));
    }

    @Test
    @DisplayName("Should authenticate user and return AuthDto")
    public void shouldAuthenticateUserAndReturnAuthDto() {
        Long userId = 1L;
        String userEmail = "test@example.com";
        String userPassword = "hashedPassword";
        Users user = Users.builder()
                .id(userId)
                .email(userEmail)
                .password(userPassword)
                .build();
        when(usersRepository.findById(userId)).thenReturn(Optional.of(user));

        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        AuthDto authDto = AuthDto.builder()
//                .id(userId)
                .email(userEmail)
                .password("password")
                .build();

        AuthDto result = usersService.auth(authDto);

        assertNotNull(result);
//        assertEquals(userId, result.getId());
        assertEquals(userEmail, result.getEmail());
        assertNotNull(result.getToken());
    }

    @Test
    @DisplayName("Should throw exception for invalid password")
    public void shouldThrowExceptionForInvalidPassword() {
        Long userId = 1L;
        String userEmail = "test@example.com";
        String userPassword = "hashedPassword";
        Users user = Users.builder()
                .id(userId)
                .email(userEmail)
                .password(userPassword)
                .build();
        when(usersRepository.findById(userId)).thenReturn(Optional.of(user));

        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        AuthDto authDto = AuthDto.builder()
//                .id(userId)
                .email(userEmail)
                .password("wrongPassword")
                .build();

        assertThrows(RuntimeException.class, () -> {
            usersService.auth(authDto);
        });
    }
}
