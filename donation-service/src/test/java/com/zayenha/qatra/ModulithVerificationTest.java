package com.zayenha.qatra;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModulithVerificationTest {

    static final ApplicationModules MODULES = ApplicationModules.of(QatraApplication.class);

    @Test
    void verifiesModuleStructure() {
        MODULES.verify();
    }

    @Test
    void printsModules() {
        System.out.println("\n=== Detected Modules ===");
        MODULES.forEach(m -> {
            System.out.println("  - " + m.getName() + " (" + m.getBasePackage() + ")");
            System.out.println("    exposed packages: " + m.getNamedInterfaces());
        });
    }
}
