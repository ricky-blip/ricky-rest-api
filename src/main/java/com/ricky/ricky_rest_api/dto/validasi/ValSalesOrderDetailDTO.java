package com.ricky.ricky_rest_api.dto.validasi;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ValSalesOrderDetailDTO {

	@NotNull(message = "ID Barang wajib diisi")
	private Long idBarang;

	@NotBlank(message = "Alamat tujuan wajib diisi")
	@Size(max = 255, message = "Alamat tujuan maksimal 255 karakter")
	private String address;

	@NotNull(message = "Quantity wajib diisi")
	@Min(value = 1, message = "Quantity minimal 1")
	private Integer quantity;

	@NotNull(message = "Harga jual wajib diisi")
	@DecimalMin(value = "0.01", message = "Harga jual harus lebih besar dari 0")
	private BigDecimal hargaJual;

	// --- Getter & Setter ---

	public Long getIdBarang() {
		return idBarang;
	}

	public void setIdBarang(Long idBarang) {
		this.idBarang = idBarang;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
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
}