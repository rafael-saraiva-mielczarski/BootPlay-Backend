package br.com.sysmap.bootcamp.web;

import br.com.sysmap.bootcamp.domain.entities.Users;
import br.com.sysmap.bootcamp.domain.service.UsersService;
import br.com.sysmap.bootcamp.dto.AuthDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class UsersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private UsersController userController;

    @Mock
    private UsersService usersService;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should return users when valid users is saved")
    public void shouldReturnUsersWhenValidUsersIsSaved() throws Exception {
        Users expectedUser = Users.builder()
                .id(1L)
                .name("teste")
                .email("test")
                .password("teste")
                .build();

        when(usersService.createUser(any(Users.class))).thenReturn(expectedUser);

        mockMvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expectedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("teste"))
                .andExpect(jsonPath("$.email").value("test"));
    }

    @Test
    @DisplayName("Should return all users")
    public void shouldReturnAllUsers() throws Exception {
        Users user1 = Users.builder()
                .id(1L)
                .name("teste")
                .email("test")
                .password("teste")
                .build();

        Users user2 = Users.builder()
                .id(2L)
                .name("anotherTest")
                .email("another@test.com")
                .password("anotherPassword")
                .build();
        List<Users> usersList = new ArrayList<>();
        usersList.add(user1);
        usersList.add(user2);
        when(usersService.getUsers()).thenReturn(usersList);

        mockMvc.perform(get("/users/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("teste"))
                .andExpect(jsonPath("$[0].email").value("test"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("anotherTest"))
                .andExpect(jsonPath("$[1].email").value("another@test.com"));

        verify(usersService, times(1)).getUsers();
    }


    @Test
    @DisplayName("Should return user by ID")
    public void shouldReturnUserById() throws Exception {
        Long userId = 1L;
        Users user = Users.builder().id(userId).name("teste").email("test").password("teste").build();
        when(usersService.getUserById(userId)).thenReturn(user);

        mockMvc.perform(get("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.name").value(user.getName()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.password").exists())
                .andExpect(jsonPath("$.password").isString());

        verify(usersService, times(1)).getUserById(userId);
    }

    @Test
    @DisplayName("Should update user successfully")
    public void shouldUpdateUserSuccessfully() throws Exception {
        Users userToUpdate = Users.builder()
                .id(1L)
                .name("updatedName")
                .email("updatedEmail@example.com")
                .password("updatedPassword")
                .build();

        when(usersService.updateUser(any(Users.class))).thenReturn(userToUpdate);

        ResponseEntity<Users> response = userController.updateUser(userToUpdate);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userToUpdate, response.getBody());
        verify(usersService).updateUser(userToUpdate);
    }

    @Test
    @DisplayName("Should not update user when user is not found")
    public void updateUserIsNotFound() throws Exception {
        Users userToUpdate = Users.builder()
                .id(1L)
                .name("updatedName")
                .email("updatedEmail@example.com")
                .password("updatedPassword")
                .build();

        when(usersService.updateUser(any(Users.class))).thenReturn(null);

        ResponseEntity<Users> response = userController.updateUser(userToUpdate);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(usersService).updateUser(userToUpdate);
    }

    @Test
    @DisplayName("Should authenticate user successfully")
    public void ShouldAuthenticateUserSuccessfully() throws Exception {
        AuthDto userToAuthenticate = mock(AuthDto.class);
        AuthDto authenticatedUser = mock(AuthDto.class);

        when(usersService.auth(any(AuthDto.class))).thenReturn(authenticatedUser);

        ResponseEntity<AuthDto> response = userController.auth(userToAuthenticate);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(authenticatedUser, response.getBody());
    }
}