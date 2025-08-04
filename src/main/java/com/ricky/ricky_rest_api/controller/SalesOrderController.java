package com.ricky.ricky_rest_api.controller;

import com.ricky.ricky_rest_api.dto.validasi.ValSalesOrderDTO;
import com.ricky.ricky_rest_api.service.SalesOrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales-orders")
public class SalesOrderController {
	@Autowired
	private SalesOrderService salesOrderService;

	@PostMapping("/create")
	@PreAuthorize("hasAnyRole('SALES', 'SALES_MANAGER')")
	public ResponseEntity<Object> createSalesOrder(
			@Valid @RequestBody ValSalesOrderDTO dto,
			HttpServletRequest request) {
		return salesOrderService.save(dto, request);
	}

	@GetMapping("/drafts")
	@PreAuthorize("hasAnyRole('SALES', 'SALES_MANAGER')")
	public ResponseEntity<Object> getDraftSalesOrders(HttpServletRequest request) {
		return salesOrderService.findAll(request);
	}
}