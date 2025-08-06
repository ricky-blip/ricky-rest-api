package com.ricky.ricky_rest_api.controller;

import com.ricky.ricky_rest_api.dto.validasi.ValSalesOrderDTO;
import com.ricky.ricky_rest_api.dto.validasi.ValSalesOrderEditDTO;
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

	/**
	 * Simpan Input -> Draft (status PENDING)
	 */
	@PostMapping("/draft")
	@PreAuthorize("hasRole('SALES')")
	public ResponseEntity<Object> saveAsDraft(
			@Valid @RequestBody ValSalesOrderDTO dto,
			HttpServletRequest request) {
		return salesOrderService.save(dto, request);
	}

	/**
	 * Get All draft (status PENDING)
	 */
	@GetMapping("/drafts")
	@PreAuthorize("hasAnyRole('SALES', 'SALES_MANAGER')")
	public ResponseEntity<Object> getDraftSalesOrders(HttpServletRequest request) {
		return salesOrderService.findAll(request);
	}

	/**
	 * Get detail draft (status PENDING)
	 */
	@GetMapping("/drafts/{id}")
	@PreAuthorize("hasAnyRole('SALES', 'SALES_MANAGER')")
	public ResponseEntity<Object> getDraftDetail(@PathVariable Long id, HttpServletRequest request) {
		return salesOrderService.findById(id, request);
	}

	/**
	 * Edit draft (status PENDING)
	 */
	@PutMapping("/{id}/edit")
	@PreAuthorize("hasAnyRole('SALES', 'SALES_MANAGER')")
	public ResponseEntity<Object> editSalesOrder(
			@PathVariable Long id,
			@Valid @RequestBody ValSalesOrderEditDTO dto,
			HttpServletRequest request) {
		return salesOrderService.editSalesOrder(id, dto, request);
	}

	/**
	 * Hapus draft (status PENDING)
	 */
	@DeleteMapping("/drafts/{id}")
	@PreAuthorize("hasAnyRole('SALES', 'SALES_MANAGER')")
	public ResponseEntity<Object> deleteDraft(@PathVariable Long id, HttpServletRequest request) {
		return salesOrderService.delete(id, request);
	}

	/**
	 * Kirim draft ke approval (PENDING → UNVALIDATED)
	 */
	@PutMapping("/{id}/submit")
	@PreAuthorize("hasRole('SALES')")
	public ResponseEntity<Object> submitToApproval(@PathVariable Long id, HttpServletRequest request) {
		return salesOrderService.submitToApproval(id, request);
	}

	/**
	 * Data UNVALIDATED
	 */
	@GetMapping("/unvalidated")
	@PreAuthorize("hasRole('SALES_MANAGER')")
	public ResponseEntity<Object> getUnvalidatedOrders(HttpServletRequest request) {
		return salesOrderService.findAllUnvalidated(request);
	}
}