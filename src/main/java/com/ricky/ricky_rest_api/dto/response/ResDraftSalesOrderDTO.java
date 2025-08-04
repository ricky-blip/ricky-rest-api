package com.ricky.ricky_rest_api.dto.response;

import java.math.BigDecimal;

public class ResDraftSalesOrderDTO {
	private Long idSalesOrder;
	private String noFaktur;
	private String namaCustomer;
	private String transactionType;
	private BigDecimal totalHarga;

	// Constructor
	public ResDraftSalesOrderDTO(Long idSalesOrder, String noFaktur, String namaCustomer, String transactionType, BigDecimal totalHarga) {
		this.idSalesOrder = idSalesOrder;
		this.noFaktur = noFaktur;
		this.namaCustomer = namaCustomer;
		this.transactionType = transactionType;
		this.totalHarga = totalHarga;
	}

	// Getter & Setter
	public Long getIdSalesOrder() {
		return idSalesOrder;
	}

	public void setIdSalesOrder(Long idSalesOrder) {
		this.idSalesOrder = idSalesOrder;
	}

	public String getNoFaktur() {
		return noFaktur;
	}

	public void setNoFaktur(String noFaktur) {
		this.noFaktur = noFaktur;
	}

	public String getNamaCustomer() {
		return namaCustomer;
	}

	public void setNamaCustomer(String namaCustomer) {
		this.namaCustomer = namaCustomer;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public BigDecimal getTotalHarga() {
		return totalHarga;
	}

	public void setTotalHarga(BigDecimal totalHarga) {
		this.totalHarga = totalHarga;
	}
}