package br.com.sysmap.bootcamp.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthDto {

    private String email;
    private String password;
    private String token;

    public AuthDto(String email, String password, String token) {
        this.email = email;
        this.password = password;
        this.token = token;
    }
}
