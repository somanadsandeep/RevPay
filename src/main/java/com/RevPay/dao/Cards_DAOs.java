package com.RevPay.dao;

import com.RevPay.dbconfig.DBConfig;
import com.RevPay.util.EncryptionUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Cards_DAOs {

    private static final Logger logger =
            LogManager.getLogger(Cards_DAOs.class);

    public void addCard(int userId, String cardNumber) {
        try {
            Connection con = DBConfig.getConnection();

            String encryptedCard = EncryptionUtil.encrypt(cardNumber);

            PreparedStatement ps =
                    con.prepareStatement(
                            "INSERT INTO saved_cards(user_id, encrypted_card, is_default) VALUES(?,?,?)");
            ps.setInt(1, userId);
            ps.setString(2, encryptedCard);
            ps.setString(3, "N"); // Oracle uses 'Y'/'N' instead of BOOLEAN

            ps.executeUpdate();

            logger.info("Card added successfully for user {}", userId);

        } catch (Exception e) {
            logger.error("Failed to add card for user {}", userId, e);
        }
    }

    public void viewCards(int userId) {
        try {
            Connection con = DBConfig.getConnection();
            PreparedStatement ps =
                    con.prepareStatement(
                            "SELECT card_id, encrypted_card, is_default FROM saved_cards WHERE user_id=?");
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();
            logger.info("Fetching saved cards for user {}", userId);

            while (rs.next()) {
                int cardId = rs.getInt("card_id");
                String encryptedCard = rs.getString("encrypted_card");
                String isDefault = rs.getString("is_default");

                // Display logic (decrypt if needed, but never log sensitive data)
                System.out.println("Card ID: " + cardId + " | Default: " + isDefault);
            }

        } catch (Exception e) {
            logger.error("Error while fetching cards for user {}", userId, e);
        }
    }

    public void setDefaultCard(int userId, int cardId) {
        try {
            Connection con = DBConfig.getConnection();

            PreparedStatement clear =
                    con.prepareStatement(
                            "UPDATE saved_cards SET is_default='N' WHERE user_id=?");
            clear.setInt(1, userId);
            clear.executeUpdate();

            PreparedStatement set =
                    con.prepareStatement(
                            "UPDATE saved_cards SET is_default='Y' WHERE card_id=? AND user_id=?");
            set.setInt(1, cardId);
            set.setInt(2, userId);
            set.executeUpdate();

            logger.info("Default card updated for user {}, card {}", userId, cardId);

        } catch (Exception e) {
            logger.error("Failed to set default card for user {}", userId, e);
        }
    }

    public void removeCard(int cardId, int userId) {
        try {
            Connection con = DBConfig.getConnection();
            PreparedStatement ps =
                    con.prepareStatement(
                            "DELETE FROM saved_cards WHERE card_id=? AND user_id=?");
            ps.setInt(1, cardId);
            ps.setInt(2, userId);
            ps.executeUpdate();

            logger.info("Card {} removed for user {}", cardId, userId);

        } catch (Exception e) {
            logger.error("Failed to remove card {} for user {}", cardId, userId, e);
        }
    }
}