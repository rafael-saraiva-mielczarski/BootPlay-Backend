package br.com.sysmap.bootcamp.web;

import br.com.sysmap.bootcamp.domain.entities.Wallet;
import br.com.sysmap.bootcamp.domain.service.WalletService;
import br.com.sysmap.bootcamp.dto.WalletDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;

@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@RestController
@RequestMapping("/wallet")
public class WalletController {

    private final WalletService walletService;

    @Operation(summary = "Check user wallet")
    @GetMapping("/")
    public ResponseEntity<Wallet> getUserWallet() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        Wallet wallet = walletService.findByUserEmail(userEmail);
        if (wallet != null) {
            return ResponseEntity.ok(wallet);
        } else {
            throw new RuntimeException("Wallet not found for authenticated user");
        }
    }

    @Operation(summary = "Credit value to user wallet")
    @PostMapping("/credit/{value}")
    public ResponseEntity<String> creditValueToWallet(@PathVariable BigDecimal value) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        WalletDto walletDto = new WalletDto(userEmail, value);

        walletService.credit(walletDto);

        return ResponseEntity.ok("Value credited to user wallet successfully");
    }
}
