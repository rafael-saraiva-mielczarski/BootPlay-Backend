package br.com.sysmap.bootcamp.domain.exceptions;

public class InvalidUserCredentialsException extends RuntimeException {
    public InvalidUserCredentialsException(String message) {
        super(message);
    }
}
