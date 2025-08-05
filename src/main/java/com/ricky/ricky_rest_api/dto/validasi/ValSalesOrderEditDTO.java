package com.ricky.ricky_rest_api.dto.validasi;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public class ValSalesOrderEditDTO {
	@NotNull(message = "Detail barang wajib diisi")
	private List<ValSalesOrderDetailEditDTO> details;

	public List<ValSalesOrderDetailEditDTO> getDetails() {
		return details;
	}

	public void setDetails(List<ValSalesOrderDetailEditDTO> details) {
		this.details = details;
	}
}