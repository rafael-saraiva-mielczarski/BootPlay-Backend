package br.com.sysmap.bootcamp.domain.listeners;


import br.com.sysmap.bootcamp.domain.service.WalletService;
import br.com.sysmap.bootcamp.dto.WalletDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@RabbitListener(queues = "WalletQueue")
public class WalletListener {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private  WalletService walletService;

    @RabbitHandler
    public void receive(String message) {
        try {
            final WalletDto walletDto = objectMapper.readValue(message, WalletDto.class);
            walletService.debit(walletDto);
            log.info("Debiting wallet: {}", walletDto);
        } catch (Exception e) {
            log.error("Error processing wallet debit: {}", e.getMessage());
        }
    }
}