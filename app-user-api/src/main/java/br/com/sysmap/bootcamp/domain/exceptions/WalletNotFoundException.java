package br.com.sysmap.bootcamp.domain.exceptions;

public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException(String message) {
        super(message);
    }
}
