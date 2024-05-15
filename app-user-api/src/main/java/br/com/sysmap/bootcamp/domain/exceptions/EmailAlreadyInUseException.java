package br.com.sysmap.bootcamp.domain.exceptions;

public class EmailAlreadyInUseException extends RuntimeException {
    public EmailAlreadyInUseException(String message) {
        super(message);
    }
}
