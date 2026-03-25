package com.zayenha.qatra.user.domain.port.out;

public interface PasswordEncoderPort {
    String encode(CharSequence rawPassword);
}
