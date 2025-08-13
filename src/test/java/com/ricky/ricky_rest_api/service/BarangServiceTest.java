package com.ricky.ricky_rest_api.service;

import com.ricky.ricky_rest_api.dto.validasi.ValBarangDTO;
import com.ricky.ricky_rest_api.model.Barang;
import com.ricky.ricky_rest_api.repository.BarangRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class BarangServiceTest {

	private BarangRepository barangRepository;
	private BarangService barangService;
	private HttpServletRequest request;

	@BeforeEach
	void setUp() throws Exception {
		// Buat mock BarangRepository agar tidak benar-benar akses database
		barangRepository = mock(BarangRepository.class);
		barangService = new BarangService();

		// Masukkan mock repository ke dalam BarangService (paksa lewat refleksi)
		var field = BarangService.class.getDeclaredField("barangRepository");
		field.setAccessible(true);
		field.set(barangService, barangRepository);

		// Mock HttpServletRequest (walau di sini nggak dipakai isinya)
		request = mock(HttpServletRequest.class);
	}

	// ==================== CREATE ====================

	@Test
	@DisplayName("Create Barang - Berhasil Menyimpan Data Baru")
	void testCreateBarang_Berhasil() {
		// Persiapkan DTO barang baru
		ValBarangDTO dto = new ValBarangDTO();
		dto.setKodeBarang("BRG900");
		dto.setNamaBarang("Laptop");
		dto.setSatuan("Unit");
		dto.setHarga(BigDecimal.valueOf(15000000));
		dto.setStokQty(10);
		dto.setReservedQty(0);
		dto.setAvailableQty(10);

		// Simulasikan repository: kode barang belum ada
		when(barangRepository.findByKodeBarang("BRG900"))
				.thenReturn(Optional.empty());

		// Jalankan method save()
		ResponseEntity<Object> response = barangService.save(dto, request);

		// Pastikan status 201 (Created)
		assertEquals(201, response.getStatusCodeValue());

		// Pastikan method save() di repository terpanggil sekali
		verify(barangRepository, times(1)).save(any(Barang.class));
	}

	@Test
	@DisplayName("Create Barang - Gagal Karena Kode Sudah Ada")
	void testCreateBarang_KodeSudahAda() {
		// Persiapkan DTO
		ValBarangDTO dto = new ValBarangDTO();
		dto.setKodeBarang("BRG900");

		// Simulasikan repository: kode barang sudah ada
		Barang existing = new Barang();
		existing.setKodeBarang("BRG900");
		when(barangRepository.findByKodeBarang("BRG900"))
				.thenReturn(Optional.of(existing));

		// Jalankan method save()
		ResponseEntity<Object> response = barangService.save(dto, request);

		// Pastikan status 400 (Bad Request) karena duplikat kode
		assertEquals(400, response.getStatusCodeValue());

		// Pastikan save() di repository tidak pernah dipanggil
		verify(barangRepository, never()).save(any());
	}

	@Test
	@DisplayName("Create Barang - Gagal Karena DTO Null")
	void testCreateBarang_DTONull() {
		// Kirim null ke method save()
		ResponseEntity<Object> response = barangService.save(null, request);

		// Pastikan status 400 (Bad Request) karena data kosong
		assertEquals(400, response.getStatusCodeValue());
	}

	@Test
	@DisplayName("Create Barang - Gagal Karena Exception di Repository")
	void testCreateBarang_Exception() {
		// Persiapkan DTO
		ValBarangDTO dto = new ValBarangDTO();
		dto.setKodeBarang("BRG900");

		// Simulasikan repository: findByKodeBarang melempar error (misalnya DB down)
		when(barangRepository.findByKodeBarang("BRG900"))
				.thenThrow(new RuntimeException("DB Error"));

		// Jalankan method save()
		ResponseEntity<Object> response = barangService.save(dto, request);

		// Pastikan status 500 (Server Error)
		assertEquals(500, response.getStatusCodeValue());
	}

	// ==================== GET ALL ====================

	@Test
	@DisplayName("Get All Barang - Berhasil Mengambil Data Aktif")
	void testFindAllBarang_Berhasil() {
		// Simulasikan ada 2 barang aktif
		Barang b1 = new Barang();
		b1.setIsactive(true);
		Barang b2 = new Barang();
		b2.setIsactive(true);

		when(barangRepository.findBarangByIsactiveTrue())
				.thenReturn(List.of(b1, b2));

		// Jalankan method findAll()
		ResponseEntity<Object> response = barangService.findAll(request);

		// Pastikan status 200 (OK)
		assertEquals(200, response.getStatusCodeValue());
	}

	@Test
	@DisplayName("Get All Barang - Gagal Karena Exception di Repository")
	void testFindAllBarang_Exception() {
		// Simulasikan repository: findBarangByIsactiveTrue melempar error
		when(barangRepository.findBarangByIsactiveTrue())
				.thenThrow(new RuntimeException("DB Error"));

		// Jalankan method findAll()
		ResponseEntity<Object> response = barangService.findAll(request);

		// Pastikan status 500 (Server Error)
		assertEquals(500, response.getStatusCodeValue());
	}
}
