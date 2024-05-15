package br.com.sysmap.bootcamp.domain.exceptions;

public class FetchingFailedException extends RuntimeException {
    public FetchingFailedException(String message) {
        super(message);
    }
}
