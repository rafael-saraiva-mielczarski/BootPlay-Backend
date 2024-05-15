package br.com.sysmap.bootcamp.domain.exceptions;

public class MissingFieldsException extends RuntimeException {
    public MissingFieldsException(String message) {
        super(message);
    }
}
