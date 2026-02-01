package com.RevPay.service;

import com.RevPay.dbconfig.DBConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.mockito.Mockito.*;

class BusinessAnalyticsServiceImplTest {

    private BusinessAnalyticsServiceImpl service;
    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws Exception {
        service = new BusinessAnalyticsServiceImpl();
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        // Mock DBConfig.getConnection() to return our mock connection
        MockedStatic<DBConfig> dbConfigMock = mockStatic(DBConfig.class);
        dbConfigMock.when(DBConfig::getConnection).thenReturn(mockConnection);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
    }

    @Test
    void testShowRevenueSummary() throws Exception {
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getDouble("total_revenue")).thenReturn(5000.0);

        service.showRevenueSummary(1);

        verify(mockStatement).setInt(1, 1);
        verify(mockResultSet).getDouble("total_revenue");
    }

    @Test
    void testShowOutstandingInvoices() throws Exception {
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("pending_count")).thenReturn(3);
        when(mockResultSet.getDouble("pending_amount")).thenReturn(1200.0);

        service.showOutstandingInvoices(2);

        verify(mockStatement).setInt(1, 2);
        verify(mockResultSet).getInt("pending_count");
        verify(mockResultSet).getDouble("pending_amount");
    }

    @Test
    void testShowPaymentTrends() throws Exception {
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("status")).thenReturn("SUCCESS", "FAILED");
        when(mockResultSet.getInt("count")).thenReturn(10, 2);

        service.showPaymentTrends(3);

        verify(mockStatement).setInt(1, 3);
        verify(mockResultSet, times(2)).getString("status");
        verify(mockResultSet, times(2)).getInt("count");
    }

    @Test
    void testShowTopCustomers() throws Exception {
        when(mockResultSet.next()).thenReturn(true, true, true, false);
        when(mockResultSet.getInt("sender_id")).thenReturn(101, 102, 103);
        when(mockResultSet.getDouble("total_paid")).thenReturn(2000.0, 1500.0, 1000.0);

        service.showTopCustomers(4);

        verify(mockStatement).setInt(1, 4);
        verify(mockResultSet, times(3)).getInt("sender_id");
        verify(mockResultSet, times(3)).getDouble("total_paid");
    }
}