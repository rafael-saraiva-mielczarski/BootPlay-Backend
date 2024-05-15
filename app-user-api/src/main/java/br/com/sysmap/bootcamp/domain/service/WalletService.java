package br.com.sysmap.bootcamp.domain.service;

import br.com.sysmap.bootcamp.domain.entities.Users;
import br.com.sysmap.bootcamp.domain.entities.Wallet;
import br.com.sysmap.bootcamp.domain.exceptions.WalletNotFoundException;
import br.com.sysmap.bootcamp.domain.repository.WalletRepository;
import br.com.sysmap.bootcamp.dto.WalletDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class WalletService {

    private final UsersService usersService;
    private final WalletRepository walletRepository;

    private static final Map<DayOfWeek, Long> POINTS_MAP = new HashMap<>();

    static {
        POINTS_MAP.put(DayOfWeek.SUNDAY, 25L);
        POINTS_MAP.put(DayOfWeek.MONDAY, 7L);
        POINTS_MAP.put(DayOfWeek.TUESDAY, 6L);
        POINTS_MAP.put(DayOfWeek.WEDNESDAY, 2L);
        POINTS_MAP.put(DayOfWeek.THURSDAY, 10L);
        POINTS_MAP.put(DayOfWeek.FRIDAY, 15L);
        POINTS_MAP.put(DayOfWeek.SATURDAY, 20L);
    }

    public void debit(WalletDto walletDto) {
        try {
            Users users = usersService.findByEmail(walletDto.getEmail());
            Wallet wallet = walletRepository.findByUsers(users).orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + walletDto.getEmail()));

            BigDecimal newValue = wallet.getBalance().subtract(walletDto.getValue());

            LocalDateTime lastUpdate = LocalDateTime.now();
            LocalDate dateOfPurchase = lastUpdate.toLocalDate();

            long pointsEarned = calculatePoints(dateOfPurchase);

            long newPointsBalance = wallet.getPoints() + pointsEarned;
            wallet.setPoints(newPointsBalance);

            wallet.setBalance(newValue);

            wallet.setLastUpdate(lastUpdate);

            walletRepository.save(wallet);
        } catch (Exception e) {
            throw new RuntimeException("Error processing debit: " + e.getMessage(), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Wallet findByUserEmail(String email) {
        try {
            Users user = usersService.findByEmail(email);
            return walletRepository.findByUsers(user).orElse(null);
        } catch (Exception e) {
            throw new RuntimeException("Error finding wallet by user email: " + e.getMessage(), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void credit(WalletDto walletDto) {
        try {
            Users users = usersService.findByEmail(walletDto.getEmail());
            Wallet wallet = walletRepository.findByUsers(users).orElseThrow();
            BigDecimal currentValue = wallet.getBalance();
            BigDecimal creditAmount = walletDto.getValue();

            wallet.setBalance(currentValue.add(creditAmount));
            walletRepository.save(wallet);
        } catch (Exception e) {
            throw new RuntimeException("Error processing credit: " + e.getMessage(), e);
        }
    }

    private long calculatePoints(LocalDate dateOfPurchase) {
        DayOfWeek dayOfWeek = dateOfPurchase.getDayOfWeek();
        return POINTS_MAP.getOrDefault(dayOfWeek, 0L);
    }
}
