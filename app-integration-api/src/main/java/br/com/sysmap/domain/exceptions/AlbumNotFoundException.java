package br.com.sysmap.domain.exceptions;

public class AlbumNotFoundException extends RuntimeException {
    public AlbumNotFoundException(String message) {
        super(message);
    }
}
