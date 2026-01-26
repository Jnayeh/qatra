package com.zayenha.qatra;

import com.zayenha.qatra.user.domain.port.in.UserCommandUseCases;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class QatraApplication {

    public static void main(String[] args) {
        SpringApplication.run(QatraApplication.class, args);
    }

    @Bean
    ApplicationRunner seedSuperAdmin(UserCommandUseCases seeder) {
        return args -> seeder.seedSuperAdminIfAbsent();
    }
}
