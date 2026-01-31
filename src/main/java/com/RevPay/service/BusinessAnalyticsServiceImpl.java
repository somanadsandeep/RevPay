package com.RevPay.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.RevPay.dbconfig.DBConfig;

public class BusinessAnalyticsServiceImpl implements BusinessAnalyticsService {
	private static final Logger logger = LogManager.getLogger(BusinessAnalyticsServiceImpl.class);

	public void showRevenueSummary(int businessId) {
		logger.info("Fetching revenue summary | businessId={}", businessId);

		try {
			Connection con = DBConfig.getConnection();

			String sql = "SELECT NVL(SUM(amount),0) AS total_revenue "
					+ "FROM payment_transactions WHERE receiver_id=?";

			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, businessId);

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				double revenue = rs.getDouble("total_revenue");
				logger.info("Total revenue fetched | businessId={} revenue={}", businessId, revenue);
			}

		} catch (Exception e) {
			logger.error("Failed to fetch revenue summary | businessId={}", businessId, e);
		}
	}

	public void showOutstandingInvoices(int businessId) {
		logger.info("Fetching outstanding invoices | businessId={}", businessId);

		try {
			Connection con = DBConfig.getConnection();

			String sql = "SELECT COUNT(*) AS pending_count, " + "NVL(SUM(amount),0) AS pending_amount "
					+ "FROM business_invoices " + "WHERE business_id=? AND status='UNPAID'";

			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, businessId);

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				int count = rs.getInt("pending_count");
				double amount = rs.getDouble("pending_amount");

				logger.info("Outstanding invoices fetched | businessId={} count={} amount={}", businessId, count,
						amount);
			}

		} catch (Exception e) {
			logger.error("Failed to fetch outstanding invoices | businessId={}", businessId, e);
		}
	}

	public void showPaymentTrends(int businessId) {
		logger.info("Fetching payment trends | businessId={}", businessId);

		try {
			Connection con = DBConfig.getConnection();

			String sql = "SELECT status, COUNT(*) AS count " + "FROM payment_transactions "
					+ "WHERE receiver_id=? GROUP BY status";

			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, businessId);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				logger.info("Payment trend | businessId={} status={} count={}", businessId, rs.getString("status"),
						rs.getInt("count"));
			}

		} catch (Exception e) {
			logger.error("Failed to fetch payment trends | businessId={}", businessId, e);
		}
	}

	public void showTopCustomers(int businessId) {
		logger.info("Fetching top customers | businessId={}", businessId);

		try {
			Connection con = DBConfig.getConnection();

			// Oracle 10g doesn't support LIMIT, use ROWNUM
			String sql = "SELECT sender_id, NVL(SUM(amount),0) AS total_paid " + "FROM payment_transactions "
					+ "WHERE receiver_id=? " + "GROUP BY sender_id " + "ORDER BY total_paid DESC";

			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, businessId);

			ResultSet rs = ps.executeQuery();

			int rowCount = 0;
			while (rs.next() && rowCount < 5) { // emulate LIMIT 5
				logger.info("Top customer | businessId={} customerId={} totalPaid={}", businessId,
						rs.getInt("sender_id"), rs.getDouble("total_paid"));
				rowCount++;
			}

		} catch (Exception e) {
			logger.error("Failed to fetch top customers | businessId={}", businessId, e);
		}
	}
}