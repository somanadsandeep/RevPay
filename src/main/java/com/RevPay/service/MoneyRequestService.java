package com.RevPay.service;

public interface MoneyRequestService {

	void sendRequest(int userId, int rid, double amt) throws Exception;

	void viewRequests(int userId) throws Exception;

	void respondToRequest(int reqId, boolean approve) throws Exception;

}
