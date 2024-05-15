package br.com.sysmap.domain.exceptions;

public class AlbumAlreadyBoughtException extends RuntimeException {
    public AlbumAlreadyBoughtException(String message) {
        super(message);
    }
}
