package com.ricky.ricky_rest_api.controller;

import com.ricky.ricky_rest_api.dto.validasi.ValCustomersDTO;
import com.ricky.ricky_rest_api.service.CustomersService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomersController {

	@Autowired
	private CustomersService customerService;

	@PostMapping("/createCustomer")
	@PreAuthorize("hasAnyRole('SALES', 'SALES_MANAGER')") // <-- RBAC
	public ResponseEntity<Object> createCustomer(@Valid @RequestBody ValCustomersDTO dto, HttpServletRequest request) {
		return customerService.save(dto, request);
	}

	// GET
	@GetMapping("/getAllCustomer")
	@PreAuthorize("hasAnyRole('SALES', 'SALES_MANAGER')") // <-- RBAC
	public ResponseEntity<Object> getAllCustomers(HttpServletRequest request) {
		return customerService.findAll(request);
	}

}
