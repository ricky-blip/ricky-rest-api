package com.ricky.ricky_rest_api.dto.validasi;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ValSalesOrderDetailEditDTO {
	@NotNull(message = "ID barang wajib diisi")
	private Long idBarang;

	@Min(value = 0, message = "Quantity tidak boleh negatif")
	private Integer quantity;

	@NotNull(message = "Harga jual wajib diisi")
	private BigDecimal hargaJual;

	@NotBlank(message = "Alamat tidak boleh kosong")
	private String address;

	public Long getIdBarang() {
		return idBarang;
	}

	public void setIdBarang(Long idBarang) {
		this.idBarang = idBarang;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getHargaJual() {
		return hargaJual;
	}

	public void setHargaJual(BigDecimal hargaJual) {
		this.hargaJual = hargaJual;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
}