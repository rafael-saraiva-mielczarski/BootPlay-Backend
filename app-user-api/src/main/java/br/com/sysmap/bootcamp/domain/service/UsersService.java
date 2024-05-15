package br.com.sysmap.bootcamp.domain.service;

import br.com.sysmap.bootcamp.domain.entities.Users;
import br.com.sysmap.bootcamp.domain.entities.Wallet;
import br.com.sysmap.bootcamp.domain.exceptions.EmailAlreadyInUseException;
import br.com.sysmap.bootcamp.domain.exceptions.InvalidUserCredentialsException;
import br.com.sysmap.bootcamp.domain.exceptions.MissingFieldsException;
import br.com.sysmap.bootcamp.domain.exceptions.UserNotFoundException;
import br.com.sysmap.bootcamp.domain.repository.UsersRepository;
import br.com.sysmap.bootcamp.domain.repository.WalletRepository;
import br.com.sysmap.bootcamp.dto.AuthDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsersService implements UserDetailsService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletRepository walletRepository;

    @Transactional(propagation = Propagation.REQUIRED)
    public Users createUser(Users user) {
        try {
            if (user.getName() == null || user.getEmail() == null || user.getPassword() == null ||
                    user.getName().isBlank() || user.getEmail().isBlank() || user.getPassword().isBlank()) {
                throw new MissingFieldsException("Name, email, and password are required fields");
            }

            Optional<Users> usersOptional = usersRepository.findByEmail(user.getEmail());
            if (usersOptional.isPresent()) {
                throw new EmailAlreadyInUseException("Email already in use!");
            }

            user = user.toBuilder().password(passwordEncoder.encode(user.getPassword())).build();
            user = this.usersRepository.save(user);

            Wallet wallet = new Wallet();
            wallet.setBalance(BigDecimal.valueOf(100));
            wallet.setPoints(0L);
            wallet.setLastUpdate(LocalDateTime.now());
            wallet.setUsers(user);

            walletRepository.save(wallet);

            return user;
        } catch (Exception e) {
            throw new RuntimeException("Error creating user", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<Users> getUsers() {
        try {
            List<Users> users = usersRepository.findAll();
            if (users.isEmpty()) {
                throw new UserNotFoundException("No users found");
            }
            return users;
        } catch (Exception e) {
            throw new RuntimeException("Error while fetching users: " + e.getMessage(), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Users getUserById(Long id) {
        return usersRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found!"));
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Users updateUser(Users user) {
        try {
            Optional<Users> optionalUser = usersRepository.findByEmail(user.getEmail());
            if (optionalUser.isPresent()) {
                Users existingUserToUpdate = optionalUser.get();
                String storedPassword = existingUserToUpdate.getPassword();

                if (passwordEncoder.matches(user.getPassword(), storedPassword)) {
                    user.setPassword(storedPassword);
                } else {
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                }

                existingUserToUpdate.setName(user.getName());
                existingUserToUpdate.setEmail(user.getEmail());
                existingUserToUpdate.setPassword(user.getPassword());

                return usersRepository.save(existingUserToUpdate);
            } else {
                throw new UserNotFoundException("User not found for email: " + user.getEmail());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error updating user: " + e.getMessage(), e);
        }
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Users> optionalUser = this.usersRepository.findByEmail(username);
        return optionalUser.map(users -> new User(users.getEmail(), users.getPassword(), new ArrayList<GrantedAuthority>()))
                .orElseThrow(() -> new UsernameNotFoundException("User: "+ username +" not found!"));
    }

    public Users findByEmail(String email) {
        return this.usersRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AuthDto auth(AuthDto authDto) {
        try {
            Users users = this.findByEmail(authDto.getEmail());

            if(!this.passwordEncoder.matches(authDto.getPassword(), users.getPassword())) {
                throw new InvalidUserCredentialsException("Invalid password!");
            }

            StringBuilder password = new StringBuilder().append(users.getEmail()).append(":").append(users.getPassword());

            return AuthDto.builder()
                    .email(users.getEmail())
                    .token(Base64.getEncoder().withoutPadding().encodeToString(password.toString().getBytes()))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error authenticating user", e);
        }
    }
}
