package com.agora;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModuleStructureTest {

    @Test
    void moduleDependenciesUsePublicApisWithoutCycles() {
        ApplicationModules.of(AgoraApplication.class).verify();
    }
}
