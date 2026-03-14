package com.helloworld.onlineshopping.common.utils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void testGenerateAndParseToken() {
        String token = jwtUtil.generateToken(999L, "testjwt", Map.of("roles", List.of("ROLE_BUYER")));
        assertNotNull(token);
        assertEquals(999L, jwtUtil.getUserId(token));
        assertEquals("testjwt", jwtUtil.getUsername(token));
    }

    @Test
    void testTokenNotExpired() {
        String token = jwtUtil.generateToken(1L, "user", Map.of());
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    void testValidateToken() {
        String token = jwtUtil.generateToken(1L, "user", Map.of());
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void testInvalidToken() {
        assertFalse(jwtUtil.validateToken("invalid.token.here"));
    }
}
