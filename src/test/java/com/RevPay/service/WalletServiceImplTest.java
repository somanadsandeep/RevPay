package com.RevPay.service;

import com.RevPay.dbconfig.DBConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletServiceImplTest {

    private WalletServiceImpl service;
    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws Exception {
        service = new WalletServiceImpl();
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        // Mock DBConfig.getConnection() to return our mock connection
        MockedStatic<DBConfig> dbConfigMock = mockStatic(DBConfig.class);
        dbConfigMock.when(DBConfig::getConnection).thenReturn(mockConnection);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
    }

    @Test
    void testDepositValid() throws Exception {
        when(mockStatement.executeUpdate()).thenReturn(1);

        service.deposit(1, 1000.0);

        verify(mockStatement).setDouble(1, 1000.0);
        verify(mockStatement).setInt(2, 1);
        verify(mockStatement).executeUpdate();
    }

    

    @Test
    void testWithdrawSuccess() throws Exception {
        PreparedStatement mockUpdateStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(startsWith("SELECT"))).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(startsWith("UPDATE"))).thenReturn(mockUpdateStatement);

        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getDouble("balance")).thenReturn(1000.0);

        when(mockUpdateStatement.executeUpdate()).thenReturn(1);

        service.withdraw(1, 200.0);

        verify(mockUpdateStatement).setDouble(1, 200.0);
        verify(mockUpdateStatement).setInt(2, 1);
        verify(mockUpdateStatement).executeUpdate();
    }

    @Test
    void testWithdrawInsufficientBalance() throws Exception {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getDouble("balance")).thenReturn(100.0);

        service.withdraw(1, 200.0);

        verify(mockStatement).setInt(1, 1);
        verify(mockResultSet).getDouble("balance");
        // No update should be executed
        verify(mockConnection, never()).prepareStatement(startsWith("UPDATE"));
    }

    @Test
    void testViewBalanceFound() throws Exception {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getDouble("balance")).thenReturn(500.0);

        service.viewBalance(1);

        verify(mockStatement).setInt(1, 1);
        verify(mockResultSet).getDouble("balance");
    }

    @Test
    void testViewBalanceNotFound() throws Exception {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        service.viewBalance(2);

        verify(mockStatement).setInt(1, 2);
        verify(mockResultSet).next();
    }

    @Test
    void testSendMoneyValid() throws Exception {
        when(mockStatement.executeUpdate()).thenReturn(1);

        service.sendMoney(1, 2, 300.0);

        verify(mockStatement).setInt(1, 1);
        verify(mockStatement).setInt(2, 2);
        verify(mockStatement).setDouble(3, 300.0);
        verify(mockStatement).setString(4, "TRANSFER");
        verify(mockStatement).executeUpdate();
    }

    @Test
    void testSendMoneyInvalidAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> service.sendMoney(1, 2, -100.0),
                "Negative amount should throw IllegalArgumentException");
    }
}