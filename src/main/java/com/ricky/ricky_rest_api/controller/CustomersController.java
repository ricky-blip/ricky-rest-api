package com.ricky.ricky_rest_api.controller;

import com.ricky.ricky_rest_api.dto.validasi.ValCustomersDTO;
import com.ricky.ricky_rest_api.model.Customers;
import com.ricky.ricky_rest_api.service.CustomersService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomersController {
	@Autowired
	private CustomersService customerService;

	//CREATE
	@PostMapping
	public ResponseEntity<Object> create(@Valid @RequestBody ValCustomersDTO dto, HttpServletRequest request) {
		return customerService.save(dto, request);
	}

	//GET
	@GetMapping("/getAllCustomer")
	public ResponseEntity<Object> getAllCustomers(HttpServletRequest request) {
		return customerService.findAll(request);
	}


}
