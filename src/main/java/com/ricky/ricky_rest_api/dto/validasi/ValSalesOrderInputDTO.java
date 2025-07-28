package com.ricky.ricky_rest_api.dto.validasi;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ValSalesOrderInputDTO {
	@Size(max = 50, message = "Nomor Faktur maksimal 50 karakter")
	private String noFaktur; // Boleh null untuk draft

	@NotNull(message = "ID Customer wajib diisi")
	private Long customerId;

	@NotNull(message = "Jenis Transaksi wajib diisi") // Validasi string dulu
	private String transactionType; // Akan divalidasi lebih lanjut di service

	@NotEmpty(message = "Minimal harus ada 1 item detail order")
	private List<ValSalesOrderDetailDTO> details;

	public String getNoFaktur() {
		return noFaktur;
	}

	public void setNoFaktur(String noFaktur) {
		this.noFaktur = noFaktur;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
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
