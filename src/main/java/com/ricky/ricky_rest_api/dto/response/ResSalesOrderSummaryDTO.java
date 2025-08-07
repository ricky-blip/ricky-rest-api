package com.ricky.ricky_rest_api.dto.response;

public class ResSalesOrderSummaryDTO {
	private int pending;
	private int unvalidated;
	private int validated;
	private int rejected;

	public ResSalesOrderSummaryDTO() {
		this.pending = 0;
		this.unvalidated = 0;
		this.validated = 0;
		this.rejected = 0;
	}

	// Getter & Setter
	public int getPending() { return pending; }
	public void setPending(int pending) { this.pending = pending; }

	public int getUnvalidated() { return unvalidated; }
	public void setUnvalidated(int unvalidated) { this.unvalidated = unvalidated; }

	public int getValidated() { return validated; }
	public void setValidated(int validated) { this.validated = validated; }

	public int getRejected() { return rejected; }
	public void setRejected(int rejected) { this.rejected = rejected; }
}