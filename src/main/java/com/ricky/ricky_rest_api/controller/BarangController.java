package com.ricky.ricky_rest_api.controller;

import com.ricky.ricky_rest_api.dto.validasi.ValBarangDTO;
import com.ricky.ricky_rest_api.service.BarangService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/barang")
public class BarangController {

	@Autowired
	private BarangService barangService;

	@PostMapping("/createBarang")
	@PreAuthorize("hasRole('SALES_MANAGER')")
	public ResponseEntity<Object> createBarang(@Valid @RequestBody ValBarangDTO dto, HttpServletRequest request) {
		return barangService.save(dto, request);
	}

	//GET
	@GetMapping("/getAllBarang")
	@PreAuthorize("hasAnyRole('SALES', 'SALES_MANAGER')")
	public ResponseEntity<Object> getAllBarang(HttpServletRequest request) {
		return barangService.findAll(request);
	}
}
