package com.RevPay.service;

import com.RevPay.dbconfig.DBConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.mockito.Mockito.*;

class InvoiceServiceImplTest {

    private InvoiceServiceImpl service;
    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws Exception {
        service = new InvoiceServiceImpl();
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        // Mock DBConfig.getConnection() to return our mock connection
        MockedStatic<DBConfig> dbConfigMock = mockStatic(DBConfig.class);
        dbConfigMock.when(DBConfig::getConnection).thenReturn(mockConnection);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
    }

    @Test
    void testCreateInvoice() throws Exception {
        when(mockStatement.executeUpdate()).thenReturn(1);

        service.createInvoice(1, "customer@example.com", "Item1", 100.0, "Net30");

        verify(mockStatement).setInt(1, 1);
        verify(mockStatement).setString(2, "customer@example.com");
        verify(mockStatement).setString(3, "Item1");
        verify(mockStatement).setDouble(4, 100.0);
        verify(mockStatement).setString(5, "Net30");
        verify(mockStatement).setString(6, "UNPAID");
        verify(mockStatement).executeUpdate();
    }

    @Test
    void testMarkInvoicePaid() throws Exception {
        when(mockStatement.executeUpdate()).thenReturn(1);

        service.markInvoicePaid(10);

        verify(mockStatement).setInt(1, 10);
        verify(mockStatement).executeUpdate();
    }

    @Test
    void testMarkInvoiceUnpaid() throws Exception {
        when(mockStatement.executeUpdate()).thenReturn(1);

        service.markInvoiceUnpaid(20);

        verify(mockStatement).setInt(1, 20);
        verify(mockStatement).executeUpdate();
    }

    @Test
    void testViewInvoicesWithResults() throws Exception {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("invoice_id")).thenReturn(101);
        when(mockResultSet.getString("customer_email")).thenReturn("cust@example.com");
        when(mockResultSet.getDouble("amount")).thenReturn(250.0);
        when(mockResultSet.getString("status")).thenReturn("UNPAID");

        service.viewInvoices(1);

        verify(mockStatement).setInt(1, 1);
        verify(mockResultSet).getInt("invoice_id");
        verify(mockResultSet).getString("customer_email");
        verify(mockResultSet).getDouble("amount");
        verify(mockResultSet).getString("status");
    }

    @Test
    void testViewInvoicesNoResults() throws Exception {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        service.viewInvoices(2);

        verify(mockStatement).setInt(1, 2);
        verify(mockResultSet).next();
    }
}