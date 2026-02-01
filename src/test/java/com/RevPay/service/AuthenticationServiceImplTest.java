package com.RevPay.service;

import com.RevPay.dao.UserDAO;
import com.RevPay.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthenticationServiceImplTest {

    private AuthenticationServiceImpl authService;
    private UserDAO mockUserDAO;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws Exception {
        authService = new AuthenticationServiceImpl();

        // Replace real DAO with mock
        mockUserDAO = Mockito.mock(UserDAO.class);
        mockResultSet = Mockito.mock(ResultSet.class);

        // Inject mock DAO into service using reflection
        var field = AuthenticationServiceImpl.class.getDeclaredField("userDAO");
        field.setAccessible(true);
        field.set(authService, mockUserDAO);
    }

    @Test
    void testLoginSuccess() throws Exception {
        when(mockUserDAO.getUserByEmailOrPhone("test@example.com")).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("user_id")).thenReturn(1);
        when(mockResultSet.getString("password_hash")).thenReturn(PasswordUtil.hashPassword("secret"));
        when(mockResultSet.getInt("failed_attempts")).thenReturn(0);
        when(mockResultSet.getString("locked")).thenReturn("N");

        int result = authService.login("test@example.com", "secret");
        assertEquals(1, result, "Login should succeed and return userId");
    }

    @Test
    void testLoginFailsWrongPassword() throws Exception {
        when(mockUserDAO.getUserByEmailOrPhone("test@example.com")).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("user_id")).thenReturn(1);
        when(mockResultSet.getString("password_hash")).thenReturn(PasswordUtil.hashPassword("secret"));
        when(mockResultSet.getInt("failed_attempts")).thenReturn(1);
        when(mockResultSet.getString("locked")).thenReturn("N");

        int result = authService.login("test@example.com", "wrongPass");
        assertEquals(-1, result, "Login should fail with wrong password");
    }

    @Test
    void testLoginFailsLockedAccount() throws Exception {
        when(mockUserDAO.getUserByEmailOrPhone("locked@example.com")).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("user_id")).thenReturn(2);
        when(mockResultSet.getString("password_hash")).thenReturn(PasswordUtil.hashPassword("secret"));
        when(mockResultSet.getInt("failed_attempts")).thenReturn(3);
        when(mockResultSet.getString("locked")).thenReturn("Y");

        int result = authService.login("locked@example.com", "secret");
        assertEquals(-1, result, "Login should fail for locked account");
    }

    @Test
    void testLoginFailsUserNotFound() throws Exception {
        when(mockUserDAO.getUserByEmailOrPhone("unknown@example.com")).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        int result = authService.login("unknown@example.com", "secret");
        assertEquals(-1, result, "Login should fail if user not found");
    }
}