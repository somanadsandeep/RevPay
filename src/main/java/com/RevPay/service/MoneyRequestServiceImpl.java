package com.RevPay.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.RevPay.dao.TransactionDAO;
import com.RevPay.dbconfig.DBConfig;

public class MoneyRequestServiceImpl implements MoneyRequestService {
    private static final Logger logger = LogManager.getLogger(MoneyRequestServiceImpl.class);

    private final WalletService walletService = new WalletServiceImpl();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    //.. SEND REQUEST ..//
    public void sendRequest(int senderId, int receiverId, double amount) throws Exception {
        requestMoney(senderId, receiverId, amount);
        System.out.println("Money request sent successfully");
    }

    //.. CORE DB METHOD ..//
    public void requestMoney(int fromUser, int toUser, double amount) throws Exception {
        if (amount <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }

        Connection con = DBConfig.getConnection();

        String sql =
                "INSERT INTO payment_requests (from_user, to_user, amount, status) " +
                        "VALUES (?, ?, ?, 'PENDING')";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, fromUser);
        ps.setInt(2, toUser);
        ps.setDouble(3, amount);

        ps.executeUpdate();
    }

    //.. VIEW REQUESTS ..//
    public void viewRequests(int userId) throws Exception {
        Connection con = DBConfig.getConnection();

        String sql =
                "SELECT request_id, from_user, amount, status " +
                        "FROM payment_requests WHERE to_user=?";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, userId);

        ResultSet rs = ps.executeQuery();

        boolean found = false;
        System.out.println("\n--- Incoming Money Requests ---");

        while (rs.next()) {
            found = true;
            System.out.println(
                    "Request ID: " + rs.getInt("request_id") +
                            " | From User: " + rs.getInt("from_user") +
                            " | Amount: " + rs.getDouble("amount") +
                            " | Status: " + rs.getString("status")
            );
        }

        if (!found) {
            System.out.println("No requests found");
        }
    }

    //.. RESPOND TO REQUEST ..//
    public void respondToRequest(int requestId, boolean approve) throws Exception {
        Connection con = DBConfig.getConnection();

        // 🔹 Get request details
        String fetchSql =
                "SELECT from_user, to_user, amount " +
                        "FROM payment_requests " +
                        "WHERE request_id=? AND status='PENDING'";

        PreparedStatement fetchPs = con.prepareStatement(fetchSql);
        fetchPs.setInt(1, requestId);

        ResultSet rs = fetchPs.executeQuery();

        if (!rs.next()) {
            System.out.println("No pending request found");
            return;
        }

        int fromUser = rs.getInt("from_user");
        int toUser = rs.getInt("to_user");
        double amount = rs.getDouble("amount");

        if (approve) {
            // WALLET TRANSFER
            walletService.withdraw(toUser, amount);
            walletService.deposit(fromUser, amount);

            // INSERT TRANSACTION (fixes history)
            transactionDAO.addTransaction(
                    toUser,
                    fromUser,
                    amount,
                    "REQUEST_TRANSFER",
                    "Money request approved"
            );
        }

        // UPDATE REQUEST STATUS
        String updateSql =
                "UPDATE payment_requests SET status=? WHERE request_id=? AND status='PENDING'";

        PreparedStatement updatePs = con.prepareStatement(updateSql);
        updatePs.setString(1, approve ? "APPROVED" : "REJECTED");
        updatePs.setInt(2, requestId);

        updatePs.executeUpdate();

        System.out.println(
                approve
                        ? "Request APPROVED and money transferred"
                        : "Request REJECTED"
        );
    }
}