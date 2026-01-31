package com.RevPay.service;

public interface AuthenticationService {

	void registerPersonal(String name, String email, String phone, String password, String pin, String question,
			String answer);

	void registerBusiness(String name, String email, String phone, String password, String pin, String businessName,
			String taxId, String address, String question, String answer);

	int login(String loginInput, String password);

	boolean recoverPassword(String email, String ans, String nextLine);

}
