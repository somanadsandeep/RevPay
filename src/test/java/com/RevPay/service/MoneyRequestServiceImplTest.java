package com.RevPay.service;

import com.RevPay.dao.TransactionDAO;
import com.RevPay.dbconfig.DBConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.mockito.Mockito.*;

class MoneyRequestServiceImplTest {

    private MoneyRequestServiceImpl service;
    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;
    private WalletService mockWalletService;
    private TransactionDAO mockTransactionDAO;

    @BeforeEach
    void setUp() throws Exception {
        service = new MoneyRequestServiceImpl();

        // Replace real dependencies with mocks using reflection
        mockWalletService = mock(WalletService.class);
        mockTransactionDAO = mock(TransactionDAO.class);

        var walletField = MoneyRequestServiceImpl.class.getDeclaredField("walletService");
        walletField.setAccessible(true);
        walletField.set(service, mockWalletService);

        var txnField = MoneyRequestServiceImpl.class.getDeclaredField("transactionDAO");
        txnField.setAccessible(true);
        txnField.set(service, mockTransactionDAO);

        // Mock JDBC
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        MockedStatic<DBConfig> dbConfigMock = mockStatic(DBConfig.class);
        dbConfigMock.when(DBConfig::getConnection).thenReturn(mockConnection);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
    }

    @Test
    void testSendRequestValid() throws Exception {
        when(mockStatement.executeUpdate()).thenReturn(1);

        service.sendRequest(1, 2, 100.0);

        verify(mockStatement).setInt(1, 1);
        verify(mockStatement).setInt(2, 2);
        verify(mockStatement).setDouble(3, 100.0);
        verify(mockStatement).executeUpdate();
    }

    

    @Test
    void testViewRequestsWithResults() throws Exception {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("request_id")).thenReturn(101);
        when(mockResultSet.getInt("from_user")).thenReturn(5);
        when(mockResultSet.getDouble("amount")).thenReturn(250.0);
        when(mockResultSet.getString("status")).thenReturn("PENDING");

        service.viewRequests(2);

        verify(mockStatement).setInt(1, 2);
        verify(mockResultSet).getInt("request_id");
        verify(mockResultSet).getInt("from_user");
        verify(mockResultSet).getDouble("amount");
        verify(mockResultSet).getString("status");
    }

    @Test
    void testViewRequestsNoResults() throws Exception {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        service.viewRequests(3);

        verify(mockStatement).setInt(1, 3);
        verify(mockResultSet).next();
    }

    @Test
    void testRespondToRequestApprove() throws Exception {
        PreparedStatement mockUpdateStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(startsWith("SELECT"))).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(startsWith("UPDATE"))).thenReturn(mockUpdateStatement);

        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("from_user")).thenReturn(1);
        when(mockResultSet.getInt("to_user")).thenReturn(2);
        when(mockResultSet.getDouble("amount")).thenReturn(100.0);

        when(mockUpdateStatement.executeUpdate()).thenReturn(1);

        service.respondToRequest(10, true);

        verify(mockWalletService).withdraw(2, 100.0);
        verify(mockWalletService).deposit(1, 100.0);
        verify(mockTransactionDAO).addTransaction(2, 1, 100.0,
                "REQUEST_TRANSFER", "Money request approved");
        verify(mockUpdateStatement).setString(1, "APPROVED");
        verify(mockUpdateStatement).setInt(2, 10);
        verify(mockUpdateStatement).executeUpdate();
    }

    @Test
    void testRespondToRequestReject() throws Exception {
        PreparedStatement mockUpdateStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(startsWith("SELECT"))).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(startsWith("UPDATE"))).thenReturn(mockUpdateStatement);

        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("from_user")).thenReturn(1);
        when(mockResultSet.getInt("to_user")).thenReturn(2);
        when(mockResultSet.getDouble("amount")).thenReturn(100.0);

        when(mockUpdateStatement.executeUpdate()).thenReturn(1);

        service.respondToRequest(20, false);

        verify(mockWalletService, never()).withdraw(anyInt(), anyDouble());
        verify(mockWalletService, never()).deposit(anyInt(), anyDouble());
        verify(mockTransactionDAO, never()).addTransaction(anyInt(), anyInt(), anyDouble(), anyString(), anyString());
        verify(mockUpdateStatement).setString(1, "REJECTED");
        verify(mockUpdateStatement).setInt(2, 20);
        verify(mockUpdateStatement).executeUpdate();
    }

    @Test
    void testRespondToRequestNoPending() throws Exception {
        when(mockConnection.prepareStatement(startsWith("SELECT"))).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        service.respondToRequest(30, true);

        verify(mockResultSet).next();
        // No wallet or transaction calls should happen
        verify(mockWalletService, never()).withdraw(anyInt(), anyDouble());
        verify(mockTransactionDAO, never()).addTransaction(anyInt(), anyInt(), anyDouble(), anyString(), anyString());
    }
}