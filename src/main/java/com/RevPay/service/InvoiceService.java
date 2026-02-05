package com.RevPay.service;

public interface InvoiceService {
void createInvoice(int businessId, String customerEmail, String items, double amount, String paymentTerms) throws Exception;
    void markInvoicePaid(int invoiceId) throws Exception;
    void markInvoiceUnpaid(int invoiceId) throws Exception;
    void viewInvoices(int businessId) throws Exception;
    void manageInvoices(int businessId) throws Exception;

}
