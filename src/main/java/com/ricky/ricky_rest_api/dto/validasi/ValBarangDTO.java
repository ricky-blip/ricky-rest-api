package com.ricky.ricky_rest_api.dto.validasi;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class ValBarangDTO {
	@NotBlank(message = "Kode barang wajib diisi")
	@Size(max = 20, message = "Kode barang maksimal 20 karakter")
	private String kodeBarang;

	@NotBlank(message = "Nama barang wajib diisi")
	@Size(max = 100, message = "Nama barang maksimal 100 karakter")
	private String namaBarang;

	@NotBlank(message = "Satuan wajib diisi")
	@Size(max = 20, message = "Satuan maksimal 20 karakter")
	private String satuan;

	@NotNull(message = "Harga wajib diisi")
	@DecimalMin(value = "0.0", inclusive = false, message = "Harga harus lebih besar dari 0")
	private BigDecimal harga; // Menggunakan BigDecimal untuk presisi harga

	@NotNull(message = "Stok quantity wajib diisi")
	@Min(value = 0, message = "Stok quantity tidak boleh negatif")
	private Integer stokQty;

	@NotNull(message = "Reserved quantity wajib diisi")
	@Min(value = 0, message = "Reserved quantity tidak boleh negatif")
	private Integer reservedQty;

	@NotNull(message = "Available quantity wajib diisi")
	@Min(value = 0, message = "Available quantity tidak boleh negatif")
	private Integer availableQty;

	@NotNull(message = "Status active wajib diisi")
	private Boolean isactive;

	public String getKodeBarang() {
		return kodeBarang;
	}

	public void setKodeBarang(String kodeBarang) {
		this.kodeBarang = kodeBarang;
	}

	public String getNamaBarang() {
		return namaBarang;
	}

	public void setNamaBarang(String namaBarang) {
		this.namaBarang = namaBarang;
	}

	public String getSatuan() {
		return satuan;
	}

	public void setSatuan(String satuan) {
		this.satuan = satuan;
	}

	public BigDecimal getHarga() {
		return harga;
	}

	public void setHarga(BigDecimal harga) {
		this.harga = harga;
	}

	public Integer getStokQty() {
		return stokQty;
	}

	public void setStokQty(Integer stokQty) {
		this.stokQty = stokQty;
	}

	public Integer getReservedQty() {
		return reservedQty;
	}

	public void setReservedQty(Integer reservedQty) {
		this.reservedQty = reservedQty;
	}

	public Integer getAvailableQty() {
		return availableQty;
	}

	public void setAvailableQty(Integer availableQty) {
		this.availableQty = availableQty;
	}

	public Boolean getIsactive() {
		return isactive;
	}

	public void setIsactive(Boolean isactive) {
		this.isactive = isactive;
	}
}
