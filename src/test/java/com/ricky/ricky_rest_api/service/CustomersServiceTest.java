package com.ricky.ricky_rest_api.service;

import com.ricky.ricky_rest_api.dto.validasi.ValCustomersDTO;
import com.ricky.ricky_rest_api.model.Customers;
import com.ricky.ricky_rest_api.repository.CustomersRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CustomersServiceTest {

	private CustomersRepository customersRepository;
	private CustomersService customersService;
	private HttpServletRequest request;

	//pakai mock ini utk supaya tidak benar-benar akses database langsung
	@BeforeEach
	void setUp() throws Exception {
		customersRepository = mock(CustomersRepository.class); // bikin object palsu (mock) untuk repository
		customersService = new CustomersService(); // bikin instance service asli (tapi repositorinya palsu)

		// Inject mock repository ke service
		var field = CustomersService.class.getDeclaredField("customersRepository");
		field.setAccessible(true); // supaya bisa akses private field
		field.set(customersService, customersRepository);

		request = mock(HttpServletRequest.class); // mock request
	}

	// ==================== CREATE ====================

	@Test
	@DisplayName("Create Customer - Berhasil Menyimpan Data Baru")
	void testCreateCustomer_Berhasil() {
		ValCustomersDTO dto = new ValCustomersDTO();
		dto.setKodeCustomer("CUST900");
		dto.setNamaCustomer("Ricky");
		dto.setAddress("Jl. Contoh");
		dto.setPhone("08123456789");
		dto.setEmail("ricky@example.com");

		when(customersRepository.findByKodeCustomer("CUST900"))
				.thenReturn(Optional.empty());

		ResponseEntity<Object> response = customersService.save(dto, request);

		assertEquals(201, response.getStatusCodeValue());
		verify(customersRepository, times(1)).save(any(Customers.class));
	}

	//ini ketika kode customer sudah ada
	@Test
	@DisplayName("Create Customer - Gagal Karena Kode Sudah Ada")
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

	//kalau data inputan null atau kosong
	@Test
	@DisplayName("Create Customer - Gagal Karena DTO Null")
	void testCreateCustomer_DTONull() {
		ResponseEntity<Object> response = customersService.save(null, request);
		assertEquals(400, response.getStatusCodeValue());
	}

	//ketika gagal akses api nya
	@Test
	@DisplayName("Create Customer - Gagal Karena Exception di Repository")
	void testCreateCustomer_Exception() {
		ValCustomersDTO dto = new ValCustomersDTO();
		dto.setKodeCustomer("CUST900");

		when(customersRepository.findByKodeCustomer("CUST900"))
				.thenThrow(new RuntimeException("DB Error"));

		ResponseEntity<Object> response = customersService.save(dto, request);
		assertEquals(500, response.getStatusCodeValue());
	}

	// ==================== GET ALL ====================
	//get semua data dengan where isactive nya true
	@Test
	@DisplayName("Get All Customer - Berhasil Mengambil Data Aktif")
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

	//ketika gagal akses api nya
	@Test
	@DisplayName("Get All Customer - Gagal Karena Exception di Repository")
	void testFindAllCustomer_Exception() {
		when(customersRepository.findCustomerByIsactiveTrue())
				.thenThrow(new RuntimeException("DB Error"));

		ResponseEntity<Object> response = customersService.findAll(request);
		assertEquals(500, response.getStatusCodeValue());
	}
}
