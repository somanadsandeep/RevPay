package com.RevPay.service;



public interface LoanService {

	void applyPersonalLoan(int userId, double amt, String nextLine);

	void repayLoan(int nextInt, double nextDouble);

	void viewLoans(int userId);
	

}
