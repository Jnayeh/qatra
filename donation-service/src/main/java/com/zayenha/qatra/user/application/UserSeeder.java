package com.zayenha.qatra.user.application;

import com.zayenha.qatra.user.domain.port.in.UserCommandUseCases;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class UserSeeder {

    @Bean
    ApplicationRunner seedSuperAdmin(UserCommandUseCases seeder) {
        return args -> seeder.seedSuperAdminIfAbsent();
    }
}
