package com.RevPay.service;

import com.RevPay.dbconfig.DBConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.mockito.Mockito.*;

class LoanServiceImplTest {

    private LoanServiceImpl service;
    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws Exception {
        service = new LoanServiceImpl();
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        // Mock DBConfig.getConnection() to return our mock connection
        MockedStatic<DBConfig> dbConfigMock = mockStatic(DBConfig.class);
        dbConfigMock.when(DBConfig::getConnection).thenReturn(mockConnection);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
    }

    @Test
    void testApplyPersonalLoan() throws Exception {
        when(mockStatement.executeUpdate()).thenReturn(1);

        service.applyPersonalLoan(1, 5000.0, "Education");

        verify(mockStatement).setInt(1, 1);
        verify(mockStatement).setString(2, "PERSONAL");
        verify(mockStatement).setDouble(3, 5000.0);
        verify(mockStatement).setDouble(4, 5000.0);
        verify(mockStatement).setString(5, "Education");
        verify(mockStatement).setString(6, "PENDING");
        verify(mockStatement).executeUpdate();
    }

    @Test
    void testApplyBusinessLoan() throws Exception {
        when(mockStatement.executeUpdate()).thenReturn(1);

        service.applyBusinessLoan(2, 10000.0, "Startup");

        verify(mockStatement).setInt(1, 2);
        verify(mockStatement).setString(2, "BUSINESS");
        verify(mockStatement).setDouble(3, 10000.0);
        verify(mockStatement).setDouble(4, 10000.0);
        verify(mockStatement).setString(5, "Startup");
        verify(mockStatement).setString(6, "PENDING");
        verify(mockStatement).executeUpdate();
    }

    @Test
    void testViewLoansWithResults() throws Exception {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("loan_id")).thenReturn(101);
        when(mockResultSet.getString("loan_type")).thenReturn("PERSONAL");
        when(mockResultSet.getDouble("amount")).thenReturn(5000.0);
        when(mockResultSet.getDouble("repayment_amount")).thenReturn(3000.0);
        when(mockResultSet.getString("status")).thenReturn("ACTIVE");
        when(mockResultSet.getString("purpose")).thenReturn("Education");

        service.viewLoans(1);

        verify(mockStatement).setInt(1, 1);
        verify(mockResultSet).getInt("loan_id");
        verify(mockResultSet).getString("loan_type");
        verify(mockResultSet).getDouble("amount");
        verify(mockResultSet).getDouble("repayment_amount");
        verify(mockResultSet).getString("status");
        verify(mockResultSet).getString("purpose");
    }

    @Test
    void testViewLoansNoResults() throws Exception {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        service.viewLoans(2);

        verify(mockStatement).setInt(1, 2);
        verify(mockResultSet).next();
    }

    @Test
    void testRepayLoanFullClear() throws Exception {
        PreparedStatement mockUpdateStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(startsWith("SELECT"))).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(startsWith("UPDATE"))).thenReturn(mockUpdateStatement);

        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getDouble("repayment_amount")).thenReturn(1000.0);

        when(mockUpdateStatement.executeUpdate()).thenReturn(1);

        service.repayLoan(10, 1000.0);

        verify(mockUpdateStatement).setInt(1, 10);
        verify(mockUpdateStatement).executeUpdate();
    }

    @Test
    void testRepayLoanPartial() throws Exception {
        PreparedStatement mockUpdateStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(startsWith("SELECT"))).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(startsWith("UPDATE"))).thenReturn(mockUpdateStatement);

        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getDouble("repayment_amount")).thenReturn(1000.0);

        when(mockUpdateStatement.executeUpdate()).thenReturn(1);

        service.repayLoan(20, 400.0);

        verify(mockUpdateStatement).setDouble(1, 600.0); // remaining
        verify(mockUpdateStatement).setInt(2, 20);
        verify(mockUpdateStatement).executeUpdate();
    }

    @Test
    void testRepayLoanInvalidAmount() throws Exception {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getDouble("repayment_amount")).thenReturn(500.0);

        service.repayLoan(30, 600.0); // invalid > remaining

        verify(mockStatement).setInt(1, 30);
        verify(mockResultSet).getDouble("repayment_amount");
        // No update should be executed
        verify(mockConnection, never()).prepareStatement(startsWith("UPDATE"));
    }
}