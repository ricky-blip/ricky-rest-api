package com.ricky.ricky_rest_api.controller;

import com.ricky.ricky_rest_api.dto.response.ResDetailSalesOrderDTO;
import com.ricky.ricky_rest_api.service.PdfService;
import com.ricky.ricky_rest_api.service.SalesOrderService;
import com.ricky.ricky_rest_api.dto.validasi.ValSalesOrderDTO;
import com.ricky.ricky_rest_api.dto.validasi.ValSalesOrderEditDTO;
import com.ricky.ricky_rest_api.util.ApiResponse;
import com.ricky.ricky_rest_api.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/api/sales-orders")
public class SalesOrderController {

	@Autowired
	private SalesOrderService salesOrderService;

	@Autowired
	private PdfService pdfService;

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
	 * Get detail untuk SEMUA status (PENDING, UNVALIDATED, VALIDATED, REJECTED)
	 */
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('SALES', 'SALES_MANAGER')")
	public ResponseEntity<Object> getDetail(@PathVariable Long id, HttpServletRequest request) {
		return salesOrderService.findById(id, request);
	}

	/**
	 * Edit draft (status PENDING)
	 */
	@PutMapping("/{id}/edit")
	@PreAuthorize("hasRole('SALES')")
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
	@PreAuthorize("hasRole('SALES')")
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
	 * GET Data UNVALIDATED
	 */
	@GetMapping("/unvalidated")
	@PreAuthorize("hasAnyRole('SALES', 'SALES_MANAGER')")
	public ResponseEntity<Object> getUnvalidatedOrders(HttpServletRequest request) {
		return salesOrderService.findAllUnvalidated(request);
	}

	/**
	 * BUTTON REJECTED
	 */
	@PutMapping("/{id}/reject")
	@PreAuthorize("hasRole('SALES_MANAGER')")
	public ResponseEntity<Object> rejectSalesOrder(@PathVariable Long id, HttpServletRequest request) {
		return salesOrderService.reject(id, request);
	}

	/**
	 * BUTTON APPROVE
	 */
	@PutMapping("/{id}/approve")
	@PreAuthorize("hasRole('SALES_MANAGER')")
	public ResponseEntity<Object> approveSalesOrder(@PathVariable Long id, HttpServletRequest request) {
		return salesOrderService.approve(id, request);
	}

	/**
	 * GET Data VALIDATED
	 */
	@GetMapping("/validated")
	@PreAuthorize("hasAnyRole('SALES', 'SALES_MANAGER')")
	public ResponseEntity<Object> getValidatedOrders(HttpServletRequest request) {
		return salesOrderService.findAllValidated(request);
	}

	/**
	 * GET Data REJECTED
	 */
	@GetMapping("/rejected")
	@PreAuthorize("hasAnyRole('SALES', 'SALES_MANAGER')")
	public ResponseEntity<Object> getRejectedOrders(HttpServletRequest request) {
		return salesOrderService.findAllRejected(request);
	}

	/**
	 * GET Data REJECTED
	 */
	@GetMapping("/summary")
	@PreAuthorize("hasAnyRole('SALES', 'SALES_MANAGER')")
	public ResponseEntity<Object> getSummary(HttpServletRequest request) {
		return salesOrderService.getSummary(request);
	}

	/**
	 * Search Data
	 */
	// controller/SalesOrderController.java
	@GetMapping("/unvalidated/search")
	@PreAuthorize("hasAnyRole('SALES', 'SALES_MANAGER')")
	public ResponseEntity<Object> searchUnvalidated(
			@RequestParam String q,
			HttpServletRequest request) {
		return salesOrderService.searchUnvalidated(q, request);
	}

	@GetMapping("/rejected/search")
	@PreAuthorize("hasAnyRole('SALES', 'SALES_MANAGER')")
	public ResponseEntity<Object> searchRejected(
			@RequestParam String q,
			HttpServletRequest request) {
		return salesOrderService.searchRejected(q, request);
	}

	@GetMapping("/validated/search")
	@PreAuthorize("hasAnyRole('SALES', 'SALES_MANAGER')")
	public ResponseEntity<Object> searchValidated(
			@RequestParam String q,
			HttpServletRequest request) {
		return salesOrderService.searchValidated(q, request);
	}

	/**
	 * Generate PDF dari Sales Order
	 */
	@GetMapping("/{id}/pdf")
	@PreAuthorize("hasAnyRole('SALES', 'SALES_MANAGER')")
	public ResponseEntity<byte[]> getPdf(@PathVariable Long id, HttpServletRequest request) {
		try {
			ResponseEntity<Object> response = salesOrderService.findById(id, request);
			if (response.getStatusCode() != HttpStatus.OK) {
				return ResponseEntity.notFound().build();
			}

			// 1. Ekstrak dari ApiResponse
			ApiResponse apiResponse = (ApiResponse) response.getBody();
			if (apiResponse == null || apiResponse.getData() == null) {
				return ResponseEntity.notFound().build();
			}

			// 2. Ambil data dan casting ke DTO
			Object data = apiResponse.getData();
			if (!(data instanceof ResDetailSalesOrderDTO)) {
				return ResponseEntity.status(500).build();
			}

			ResDetailSalesOrderDTO dto = (ResDetailSalesOrderDTO) data;

			// 3. Generate PDF
			byte[] pdfBytes = pdfService.generateSalesOrderPdf(dto);

			return ResponseUtil.file(pdfBytes, "sales-order-" + dto.getNoFaktur() + ".pdf");

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).build();
		}
	}
}