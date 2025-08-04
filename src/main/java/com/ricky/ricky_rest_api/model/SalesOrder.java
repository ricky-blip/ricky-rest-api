package com.ricky.ricky_rest_api.model; // Sesuaikan dengan package Anda

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales_order") // Pastikan nama tabel sesuai
public class SalesOrder {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_sales_order")
	private Long idSalesOrder;

	@Column(name = "no_faktur", unique = true, nullable = false)
	private String noFaktur;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_customer", nullable = false)
	private Customers customer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_user_sales_manager", nullable = true)
	private User salesManager;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_user_sales", nullable = false)
	private User salesPerson;

	@Enumerated(EnumType.STRING)
	@Column(name = "jenis_transaksi", nullable = false)
	private TransactionType transactionType;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private OrderStatus status;

	@Column(name = "subtotal", precision = 15, scale = 2, nullable = false)
	private BigDecimal subtotal;

	@Column(name = "ppn", precision = 5, scale = 2, nullable = false) // Misalnya 10.00 untuk 10%
	private BigDecimal ppn;

	@Column(name = "jumlah_ppn", precision = 15, scale = 2, nullable = false)
	private BigDecimal jumlahPpn;

	@Column(name = "total_harga", precision = 15, scale = 2, nullable = false)
	private BigDecimal totalHarga;

	@Column(name = "tanggal_order", nullable = false)
	private LocalDateTime tanggalOrder;

	@Column(name = "tanggal_validasi")
	private LocalDateTime tanggalValidasi;

	@Column(name = "created_at", updatable = false)
	@CreationTimestamp
	private LocalDateTime createdAt;

	@Column(name = "updated_at", insertable = false)
	@UpdateTimestamp
	private LocalDateTime updatedAt;

	@OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<SalesOrderDetail> details = new ArrayList<>();

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

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

	public Customers getCustomer() {
		return customer;
	}

	public void setCustomer(Customers customer) {
		this.customer = customer;
	}

	public User getSalesManager() {
		return salesManager;
	}

	public void setSalesManager(User salesManager) {
		this.salesManager = salesManager;
	}

	public User getSalesPerson() {
		return salesPerson;
	}

	public void setSalesPerson(User salesPerson) {
		this.salesPerson = salesPerson;
	}

	public TransactionType getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(TransactionType transactionType) {
		this.transactionType = transactionType;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public BigDecimal getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(BigDecimal subtotal) {
		this.subtotal = subtotal;
	}

	public BigDecimal getPpn() {
		return ppn;
	}

	public void setPpn(BigDecimal ppn) {
		this.ppn = ppn;
	}

	public BigDecimal getJumlahPpn() {
		return jumlahPpn;
	}

	public void setJumlahPpn(BigDecimal jumlahPpn) {
		this.jumlahPpn = jumlahPpn;
	}

	public BigDecimal getTotalHarga() {
		return totalHarga;
	}

	public void setTotalHarga(BigDecimal totalHarga) {
		this.totalHarga = totalHarga;
	}

	public LocalDateTime getTanggalOrder() {
		return tanggalOrder;
	}

	public void setTanggalOrder(LocalDateTime tanggalOrder) {
		this.tanggalOrder = tanggalOrder;
	}

	public LocalDateTime getTanggalValidasi() {
		return tanggalValidasi;
	}

	public void setTanggalValidasi(LocalDateTime tanggalValidasi) {
		this.tanggalValidasi = tanggalValidasi;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public List<SalesOrderDetail> getDetails() {
		return details;
	}

	public void setDetails(List<SalesOrderDetail> details) {
		if (this.details != null) {
			this.details.clear();
			if (details != null) {
				this.details.addAll(details);
			}
		} else {
			this.details = details;
		}
		if (this.details != null) {
			for (SalesOrderDetail detail : this.details) {
				detail.setSalesOrder(this);
			}
		}
	}

	public void addDetail(SalesOrderDetail detail) {
		if (this.details == null) {
			this.details = new ArrayList<>();
		}
		this.details.add(detail);
		detail.setSalesOrder(this);
	}

	public void removeDetail(SalesOrderDetail detail) {
		if (this.details != null) {
			this.details.remove(detail);
			detail.setSalesOrder(null);
		}
	}

}