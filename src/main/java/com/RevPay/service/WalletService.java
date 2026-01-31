package com.RevPay.service;

public interface WalletService {

	void withdraw(int toUser, double amount);

	void deposit(int fromUser, double amount);

	void viewBalance(int userId);

}
