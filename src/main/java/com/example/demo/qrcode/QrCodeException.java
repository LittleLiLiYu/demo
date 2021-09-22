package com.example.demo.qrcode;

/**
 * Qrcode异常
 * 
 * @author xiaoleilu
 */
public class QrCodeException extends RuntimeException {
	private static final long serialVersionUID = 8247610319171014183L;

	public QrCodeException(Throwable e) {
		super(e.getMessage(), e);
	}

	public QrCodeException(String message) {
		super(message);
	}

	public QrCodeException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
