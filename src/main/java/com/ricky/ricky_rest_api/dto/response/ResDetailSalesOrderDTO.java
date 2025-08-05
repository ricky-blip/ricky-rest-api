package com.ricky.ricky_rest_api.dto.response;

import com.ricky.ricky_rest_api.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ResDetailSalesOrderDTO {
	private String noFaktur;
	private LocalDateTime tanggalOrder;
	private String transactionType;
	private String namaCustomer;
	private String alamatCustomer;
	private String phoneCustomer;
	private String emailCustomer;

	private BigDecimal subtotal;
	private BigDecimal ppn;
	private BigDecimal jumlahPpn;
	private BigDecimal totalHarga;

	private OrderStatus status;

	private UserDTO salesPerson;
	private UserDTO salesManager;

	private List<DetailBarangDTO> details;

	// Constructor
	public ResDetailSalesOrderDTO() {}

	// Getter & Setter
	public String getNoFaktur() { return noFaktur; }
	public void setNoFaktur(String noFaktur) { this.noFaktur = noFaktur; }

	public LocalDateTime getTanggalOrder() { return tanggalOrder; }
	public void setTanggalOrder(LocalDateTime tanggalOrder) { this.tanggalOrder = tanggalOrder; }

	public String getTransactionType() { return transactionType; }
	public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

	public String getNamaCustomer() { return namaCustomer; }
	public void setNamaCustomer(String namaCustomer) { this.namaCustomer = namaCustomer; }

	public String getAlamatCustomer() { return alamatCustomer; }
	public void setAlamatCustomer(String alamatCustomer) { this.alamatCustomer = alamatCustomer; }

	public String getPhoneCustomer() { return phoneCustomer; }
	public void setPhoneCustomer(String phoneCustomer) { this.phoneCustomer = phoneCustomer; }

	public String getEmailCustomer() { return emailCustomer; }
	public void setEmailCustomer(String emailCustomer) { this.emailCustomer = emailCustomer; }

	public BigDecimal getSubtotal() { return subtotal; }
	public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

	public BigDecimal getPpn() { return ppn; }
	public void setPpn(BigDecimal ppn) { this.ppn = ppn; }

	public BigDecimal getJumlahPpn() { return jumlahPpn; }
	public void setJumlahPpn(BigDecimal jumlahPpn) { this.jumlahPpn = jumlahPpn; }

	public BigDecimal getTotalHarga() { return totalHarga; }
	public void setTotalHarga(BigDecimal totalHarga) { this.totalHarga = totalHarga; }

	public OrderStatus getStatus() { return status; }
	public void setStatus(OrderStatus status) { this.status = status; }

	public UserDTO getSalesPerson() { return salesPerson; }
	public void setSalesPerson(UserDTO salesPerson) { this.salesPerson = salesPerson; }

	public UserDTO getSalesManager() { return salesManager; }
	public void setSalesManager(UserDTO salesManager) { this.salesManager = salesManager; }

	public List<DetailBarangDTO> getDetails() { return details; }
	public void setDetails(List<DetailBarangDTO> details) { this.details = details; }
}