package br.com.sysmap.bootcamp.web;

import br.com.sysmap.bootcamp.domain.entities.Users;
import br.com.sysmap.bootcamp.domain.service.UsersService;
import br.com.sysmap.bootcamp.dto.AuthDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UsersController {

    private static final Logger log = LoggerFactory.getLogger(UsersController.class);
    private final UsersService usersService;

    @Operation(summary = "Create a new user")
    @PostMapping("/create")
    public ResponseEntity<Users> createUser(@RequestBody Users user) {
        return ResponseEntity.ok(this.usersService.createUser(user));
    }

    @Operation(summary = "Find all users created")
    @GetMapping("/")
    public ResponseEntity<List<Users>> getUsers() {
        List<Users> users = this.usersService.getUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Find a specific user by Id")
    @GetMapping("/{id}")
    public ResponseEntity<Users> getUser(@PathVariable Long id) {
        Users user = this.usersService.getUserById(id);
        if(user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Update user details")
    @PutMapping("/update")
    public ResponseEntity<Users> updateUser(@RequestBody Users user) {
        log.info("Updating user: {}", user);
        Users updatedUser = this.usersService.updateUser(user);
        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Authenticate an already created user")
    @PostMapping("/auth")
    public ResponseEntity<AuthDto> auth(@RequestBody AuthDto user) {
        return ResponseEntity.ok(this.usersService.auth(user));
    }
}
