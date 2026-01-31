package com.RevPay.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.RevPay.dbconfig.DBConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class WalletDAO {

    private static final Logger logger =
            LogManager.getLogger(WalletDAO.class);

    // Fetch wallet balance
    public double getBalance(int userId) {
        try {
            Connection con = DBConfig.getConnection();
            PreparedStatement ps =
                    con.prepareStatement(
                            "SELECT balance FROM user_wallet WHERE user_id=?");
            ps.setInt(1, userId);

            logger.info("Fetching wallet balance for user {}", userId);

            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDouble("balance") : 0;

        } catch (Exception e) {
            logger.error("Failed to fetch wallet balance for user {}", userId, e);
            return 0;
        }
    }

    // Update wallet balance
    public void updateBalance(int userId, double newBalance) {
        try {
            Connection con = DBConfig.getConnection();
            PreparedStatement ps =
                    con.prepareStatement(
                            "UPDATE user_wallet SET balance=?, last_updated=SYSTIMESTAMP WHERE user_id=?");
            ps.setDouble(1, newBalance);
            ps.setInt(2, userId);

            ps.executeUpdate();

            logger.info("Wallet balance updated for user {} | newBalance={}",
                    userId, newBalance);

        } catch (Exception e) {
            logger.error("Failed to update wallet balance for user {}", userId, e);
        }
    }
}