package com.ricky.ricky_rest_api.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "barang")
public class Barang {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_barang")
	private Integer idBarang;

	@Column(name = "kode_barang", length = 20, nullable = false, unique = true)
	private String kodeBarang;

	@Column(name = "nama_barang", length = 100, nullable = false)
	private String namaBarang;

	@Column(name = "satuan", length = 20, nullable = false)
	private String satuan;

	@Column(name = "harga", precision = 15, scale = 2, nullable = false)
	private BigDecimal harga;

	@Column(name = "stok_qty", nullable = false)
	private Integer stokQty;

	@Column(name = "reserved_qty", nullable = false)
	private Integer reservedQty;

	@Column(name = "available_qty", nullable = false)
	private Integer availableQty;

	// Mengikuti nama kolom dari file data.sql
	@Column(name = "isactive", nullable = false)
	private Boolean isactive; // Nama field mengikuti nama kolom DB

	// Mengikuti nama kolom dan penggunaan timestamp dari file data.sql
	@Column(name = "created_at", updatable = false)
	@CreationTimestamp
	private LocalDateTime createdAt;

	@Column(name = "updated_at", insertable = false)
	@UpdateTimestamp
	private LocalDateTime updatedAt;

	public Integer getIdBarang() {
		return idBarang;
	}

	public void setIdBarang(Integer idBarang) {
		this.idBarang = idBarang;
	}

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

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
