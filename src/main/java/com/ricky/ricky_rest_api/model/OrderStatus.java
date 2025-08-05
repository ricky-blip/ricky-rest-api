package com.ricky.ricky_rest_api.model;

public enum OrderStatus {
	PENDING("PENDING"),
	UNVALIDATED("UNVALIDATED"),
	VALIDATED("VALIDATED"),
	REJECTED("REJECTED");

	private final String status;

	OrderStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}
}