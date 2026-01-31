package com.RevPay.service;

public interface NotificationService {

	void notifyUser(int userId, String string);

	void viewNotifications(int userId);

}
