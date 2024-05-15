package br.com.sysmap.bootcamp.domain.service;

import br.com.sysmap.bootcamp.domain.entities.Users;
import br.com.sysmap.bootcamp.domain.entities.Wallet;
import br.com.sysmap.bootcamp.domain.repository.WalletRepository;
import br.com.sysmap.bootcamp.dto.WalletDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletServiceTest {

    @Mock
    private UsersService usersService;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletService walletService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should debit from wallet and update points when sufficient balance exists")
    void shouldDebitFromWalletAndUpdatePointsWhenSufficientBalanceExists() {
        String userEmail = "test@example.com";
        WalletDto walletDto = new WalletDto(userEmail, BigDecimal.valueOf(50));
        Users user = mock(Users.class);
        Wallet wallet = mock(Wallet.class);

        when(usersService.findByEmail(userEmail)).thenReturn(user);
        when(walletRepository.findByUsers(user)).thenReturn(Optional.of(wallet));
        when(wallet.getBalance()).thenReturn(BigDecimal.valueOf(100));
        when(wallet.getPoints()).thenReturn(0L);
        when(wallet.getLastUpdate()).thenReturn(LocalDateTime.now());

        walletService.debit(walletDto);

        verify(wallet).setBalance(BigDecimal.valueOf(50));
        verify(walletRepository, times(1)).save(wallet);
    }

    @Test
    @DisplayName("Should return wallet when user email is valid")
    public void shouldReturnWalletWhenUserEmailIsValid() {
        String userEmail = "test@example.com";
        Users user = mock(Users.class);
        Wallet expectedWallet = mock(Wallet.class);

        when(usersService.findByEmail(userEmail)).thenReturn(user);
        when(walletRepository.findByUsers(user)).thenReturn(Optional.of(expectedWallet));

        Wallet actualWallet = walletService.findByUserEmail(userEmail);

        assertEquals(expectedWallet, actualWallet);
        verify(usersService, times(1)).findByEmail(userEmail);
        verify(walletRepository, times(1)).findByUsers(user);
    }

    @Test
    @DisplayName("Should return null when user email is invalid")
    public void shouldReturnNullWhenUserEmailIsInvalid() {
        String userEmail = "nonexistent@example.com";

        when(usersService.findByEmail(userEmail)).thenReturn(null);

        Wallet actualWallet = walletService.findByUserEmail(userEmail);

        assertNull(actualWallet);
        verify(usersService, times(1)).findByEmail(userEmail);
        verify(walletRepository, never()).findByUsers(any(Users.class));
    }

    @Test
    @DisplayName("Should credit wallet balance")
    public void shouldCreditWalletBalance() {
        String userEmail = "user@example.com";
        BigDecimal currentBalance = BigDecimal.valueOf(100);
        BigDecimal creditAmount = BigDecimal.valueOf(50);
        WalletDto walletDto = new WalletDto(userEmail, creditAmount);

        Users user = mock(Users.class);
        user.setEmail(userEmail);

        Wallet wallet = new Wallet();
        wallet.setBalance(currentBalance);

        when(usersService.findByEmail(userEmail)).thenReturn(user);
        when(walletRepository.findByUsers(user)).thenReturn(Optional.of(wallet));

        walletService.credit(walletDto);

        BigDecimal expectedBalance = currentBalance.add(creditAmount);
        assertEquals(expectedBalance, wallet.getBalance());
        verify(usersService, times(1)).findByEmail(userEmail);
        verify(walletRepository, times(1)).findByUsers(user);
        verify(walletRepository, times(1)).save(wallet);
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    public void shouldThrowExceptionWhenUserNotFound() {
        String userEmail = "nonexistent@example.com";
        WalletDto walletDto = new WalletDto(userEmail, BigDecimal.TEN);

        when(usersService.findByEmail(userEmail)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> walletService.credit(walletDto));
        verify(usersService, times(1)).findByEmail(userEmail);
        verify(walletRepository, never()).findByUsers(any(Users.class));
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Should throw exception when wallet not found")
    public void shouldThrowExceptionWhenWalletNotFound() {
        String userEmail = "user@example.com";
        BigDecimal creditAmount = BigDecimal.TEN;
        WalletDto walletDto = new WalletDto(userEmail, creditAmount);

        Users user = mock(Users.class);
        user.setEmail(userEmail);

        when(usersService.findByEmail(userEmail)).thenReturn(user);
        when(walletRepository.findByUsers(user)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> walletService.credit(walletDto));
        verify(usersService, times(1)).findByEmail(userEmail);
        verify(walletRepository, times(1)).findByUsers(user);
        verify(walletRepository, never()).save(any(Wallet.class));
    }
}

