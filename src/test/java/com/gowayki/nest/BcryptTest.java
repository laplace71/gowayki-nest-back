package com.gowayki.nest;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class BcryptTest {
    @Test
    void testBcrypt() {
        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
        System.out.println("HASH_123456=" + enc.encode("123456"));
    }
}

