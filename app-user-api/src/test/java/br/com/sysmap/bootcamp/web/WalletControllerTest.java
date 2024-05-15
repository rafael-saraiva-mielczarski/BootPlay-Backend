package br.com.sysmap.bootcamp.web;

import br.com.sysmap.bootcamp.domain.entities.Wallet;
import br.com.sysmap.bootcamp.domain.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class WalletControllerTest {

    @Mock
    private WalletService walletService;

    @InjectMocks
    private WalletController walletController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should get user Wallet")
    void shouldGetUserWallet() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("user@example.com", "password");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);

        Wallet expectedWallet = new Wallet();
        expectedWallet.setId(1L);
        expectedWallet.setBalance(BigDecimal.valueOf(100));

        when(walletService.findByUserEmail("user@example.com")).thenReturn(expectedWallet);

        WalletController walletController = new WalletController(walletService);

        ResponseEntity<Wallet> response = walletController.getUserWallet();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedWallet, response.getBody());
    }

    @Test
    @DisplayName("Should get wallet not found")
    void shouldGetWalletNotFound() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("user@example.com", "password");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);

        when(walletService.findByUserEmail("user@example.com")).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            walletController.getUserWallet();
        });
    }

    @Test
    @DisplayName("Should get wallet for authenticated user")
    void shouldGetWalletForAuthenticatedUser() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("user@example.com", "password");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);

        assertEquals("user@example.com", authentication.getName());
    }

    @Test
    @DisplayName("Should add credit to wallet")
    void shouldAddCreditToWallet() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("user@example.com", "password");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);

        BigDecimal value = BigDecimal.valueOf(50);

        ResponseEntity<String> response = walletController.creditValueToWallet(value);

        assertEquals("Value credited to user wallet successfully", response.getBody());
    }
}

