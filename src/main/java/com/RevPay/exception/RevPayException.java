package com.RevPay.exception;

public class RevPayException extends RuntimeException{
	public RevPayException() {
		super("RevPay banking exception called");
	}
	public RevPayException(String message) {
		super(message);
		
	}
}
