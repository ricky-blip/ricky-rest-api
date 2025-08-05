package com.ricky.ricky_rest_api.dto.response;

import java.math.BigDecimal;

public class DetailBarangDTO {
	private String kodeBarang;
	private String namaBarang;
	private String satuan;
	private Integer quantity;
	private BigDecimal hargaJual;
	private String address;
	private BigDecimal subtotal;

	public DetailBarangDTO() {}

	// Constructor
	public DetailBarangDTO(String kodeBarang, String namaBarang, String satuan, Integer quantity, BigDecimal hargaJual, String address) {
		this.kodeBarang = kodeBarang;
		this.namaBarang = namaBarang;
		this.satuan = satuan;
		this.quantity = quantity;
		this.hargaJual = hargaJual;
		this.address = address;
		this.subtotal = hargaJual.multiply(BigDecimal.valueOf(quantity));
	}

	// Getter & Setter
	public String getKodeBarang() { return kodeBarang; }
	public void setKodeBarang(String kodeBarang) { this.kodeBarang = kodeBarang; }

	public String getNamaBarang() { return namaBarang; }
	public void setNamaBarang(String namaBarang) { this.namaBarang = namaBarang; }

	public String getSatuan() { return satuan; }
	public void setSatuan(String satuan) { this.satuan = satuan; }

	public Integer getQuantity() { return quantity; }
	public void setQuantity(Integer quantity) { this.quantity = quantity; }

	public BigDecimal getHargaJual() { return hargaJual; }
	public void setHargaJual(BigDecimal hargaJual) { this.hargaJual = hargaJual; }

	public String getAddress() { return address; }
	public void setAddress(String address) { this.address = address; }

	public BigDecimal getSubtotal() { return subtotal; }
	public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}