package com.helloworld.onlineshopping.modules.auth;

import com.helloworld.onlineshopping.common.exception.BusinessException;
import com.helloworld.onlineshopping.modules.auth.dto.LoginDTO;
import com.helloworld.onlineshopping.modules.auth.dto.RegisterDTO;
import com.helloworld.onlineshopping.modules.auth.service.AuthService;
import com.helloworld.onlineshopping.modules.auth.vo.LoginVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    private RegisterDTO buildRegister(String username) {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername(username);
        dto.setPassword("test123456");
        dto.setNickname("Test " + username);
        return dto;
    }

    @Test
    void testRegisterSuccess() {
        authService.register(buildRegister("testuser_reg"));
        // If no exception, registration succeeded
    }

    @Test
    void testRegisterDuplicateUsername() {
        authService.register(buildRegister("testuser_dup"));
        assertThrows(BusinessException.class, () -> authService.register(buildRegister("testuser_dup")));
    }

    @Test
    void testLoginSuccess() {
        authService.register(buildRegister("testuser_login"));
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser_login");
        loginDTO.setPassword("test123456");
        LoginVO vo = authService.login(loginDTO);
        assertNotNull(vo.getToken());
        assertEquals("testuser_login", vo.getUsername());
        assertTrue(vo.getRoles().contains("ROLE_BUYER"));
    }

    @Test
    void testLoginWrongPassword() {
        authService.register(buildRegister("testuser_wrongpw"));
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser_wrongpw");
        loginDTO.setPassword("wrongpassword");
        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(loginDTO));
        assertEquals(401, ex.getCode());
    }
}
