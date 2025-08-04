package com.ricky.ricky_rest_api.dto.validasi;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public class ValSalesOrderDTO {

	@NotNull(message = "ID Customer wajib diisi")
	private Long idCustomer;

	@NotBlank(message = "Jenis transaksi wajib diisi")
	@Size(max = 10, message = "Jenis transaksi maksimal 10 karakter")
	private String transactionType;

	@NotEmpty(message = "Detail order tidak boleh kosong")
	private List<ValSalesOrderDetailDTO> details;

	// --- Getter & Setter ---

	public Long getIdCustomer() {
		return idCustomer;
	}

	public void setIdCustomer(Long idCustomer) {
		this.idCustomer = idCustomer;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public List<ValSalesOrderDetailDTO> getDetails() {
		return details;
	}

	public void setDetails(List<ValSalesOrderDetailDTO> details) {
		this.details = details;
	}
}