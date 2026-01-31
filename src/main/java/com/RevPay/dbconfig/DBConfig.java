package com.RevPay.dbconfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBConfig {
	 public static final String DRIVER = "oracle.jdbc.driver.OracleDriver";
	    public static final String url = "jdbc:oracle:thin:@localhost:1521:XE";
	    public static final String username = "sandeep";
	    public static final String password = "12345";
		public static Connection getConnection() {
			try {
				return DriverManager.getConnection(url,username,password);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return null;
			
			
		}

}