package com.ricky.ricky_rest_api.service;

import com.ricky.ricky_rest_api.dto.validasi.ValCustomersDTO;
import com.ricky.ricky_rest_api.model.Customers;
import com.ricky.ricky_rest_api.repository.CustomersRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomersServiceTest {

	private CustomersRepository customersRepository;
	private CustomersService customersService;
	private HttpServletRequest request;


	@BeforeEach
	void setUp() throws Exception {
		customersRepository = mock(CustomersRepository.class);
		customersService = new CustomersService();

		// Inject mock repository ke service
		var field = CustomersService.class.getDeclaredField("customersRepository");
		field.setAccessible(true);
		field.set(customersService, customersRepository);

		request = mock(HttpServletRequest.class);
	}

	//Unit Test Buat Customer Baru
	@Test
	void testCreateCustomer_Berhasil() {
		ValCustomersDTO dto = new ValCustomersDTO();
		dto.setKodeCustomer("CUST900");
		dto.setNamaCustomer("Ricky");
		dto.setAddress("Jl. Contoh");
		dto.setPhone("08123456789");
		dto.setEmail("ricky@example.com");

		when(customersRepository.findByKodeCustomer("C001"))
				.thenReturn(Optional.empty());

		ResponseEntity<Object> response = customersService.save(dto, request);

		assertEquals(201, response.getStatusCodeValue());
		verify(customersRepository, times(1)).save(any(Customers.class));
	}

	//Unit Test Buat Cek Kode Customer yang sudah ada
	@Test
	void testCreateCustomer_KodeSudahAda() {
		ValCustomersDTO dto = new ValCustomersDTO();
		dto.setKodeCustomer("CUST900");

		Customers existing = new Customers();
		existing.setKodeCustomer("CUST900");

		when(customersRepository.findByKodeCustomer("CUST900"))
				.thenReturn(Optional.of(existing));

		ResponseEntity<Object> response = customersService.save(dto, request);

		assertEquals(400, response.getStatusCodeValue());
		verify(customersRepository, never()).save(any());
	}

	//Unit Test Buat inputan yang null atau kosong
	@Test
	void testCreateCustomer_DTONull() {
		ResponseEntity<Object> response = customersService.save(null, request);
		assertEquals(400, response.getStatusCodeValue());
	}

	//Unit Test Buat Ambil Semua data Customers
	@Test
	void testFindAllCustomer_Berhasil() {
		Customers c1 = new Customers();
		c1.setIsactive(true);
		Customers c2 = new Customers();
		c2.setIsactive(true);

		when(customersRepository.findCustomerByIsactiveTrue())
				.thenReturn(List.of(c1, c2));

		ResponseEntity<Object> response = customersService.findAll(request);
		assertEquals(200, response.getStatusCodeValue());
	}
}
