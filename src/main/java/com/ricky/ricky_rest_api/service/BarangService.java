package com.ricky.ricky_rest_api.service;

import com.ricky.ricky_rest_api.core.IService;
import com.ricky.ricky_rest_api.dto.validasi.ValBarangDTO;
import com.ricky.ricky_rest_api.model.Barang;
import com.ricky.ricky_rest_api.repository.BarangRepository;
import com.ricky.ricky_rest_api.util.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BarangService implements IService<ValBarangDTO> {

	@Autowired
	private BarangRepository barangRepository;

	private final String className = "BarangService";

	@Override
	public ResponseEntity<Object> save(ValBarangDTO dto, HttpServletRequest request) {
		if (dto == null) {
			return ResponseUtil.badRequest("Data barang tidak boleh kosong");
		}

		try {
			// Validasi: kodeBarang harus unik
			if (barangRepository.findByKodeBarang(dto.getKodeBarang()).isPresent()) {
				return ResponseUtil.badRequest("Kode barang sudah ada");
			}

			Barang barang = new Barang();
			barang.setKodeBarang(dto.getKodeBarang());
			barang.setNamaBarang(dto.getNamaBarang());
			barang.setSatuan(dto.getSatuan());
			barang.setHarga(dto.getHarga());
			barang.setStokQty(dto.getStokQty());
			barang.setReservedQty(dto.getReservedQty());
			barang.setAvailableQty(dto.getAvailableQty());
			barang.setIsactive(true);

			barangRepository.save(barang);

			return ResponseUtil.created("Data barang berhasil disimpan", null);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal menyimpan barang");
		}
	}

	@Override
	public ResponseEntity<Object> update(Long id, ValBarangDTO dto, HttpServletRequest request) {
		try {
			Barang barang = barangRepository.findById(id)
					.orElseThrow(() -> new RuntimeException("Barang tidak ditemukan"));

			// Cek unik kodeBarang (kecuali milik dirinya sendiri)
			if (!barang.getKodeBarang().equals(dto.getKodeBarang())) {
				if (barangRepository.findByKodeBarang(dto.getKodeBarang()).isPresent()) {
					return ResponseUtil.badRequest("Kode barang sudah digunakan");
				}
			}

			barang.setKodeBarang(dto.getKodeBarang());
			barang.setNamaBarang(dto.getNamaBarang());
			barang.setSatuan(dto.getSatuan());
			barang.setHarga(dto.getHarga());
			barang.setStokQty(dto.getStokQty());
			barang.setReservedQty(dto.getReservedQty());
			barang.setAvailableQty(dto.getAvailableQty());

			barangRepository.save(barang);

			return ResponseUtil.success("Data barang berhasil diupdate", null);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal mengupdate barang");
		}
	}

	@Override
	public ResponseEntity<Object> delete(Long id, HttpServletRequest request) {
		try {
			Barang barang = barangRepository.findById(id)
					.orElseThrow(() -> new RuntimeException("Barang tidak ditemukan"));

			barang.setIsactive(false); // Soft delete
			barangRepository.save(barang);

			return ResponseUtil.success("Data barang berhasil dihapus", null);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal menghapus barang");
		}
	}

	@Override
	public ResponseEntity<Object> findAll(HttpServletRequest request) {
		try {
			List<Barang> activeBarang = barangRepository.findBarangByIsactiveTrue();
			return ResponseUtil.success("Data barang ditemukan", activeBarang);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal mengambil data barang");
		}
	}

	@Override
	public ResponseEntity<Object> findById(Long id, HttpServletRequest request) {
		try {
			Barang barang = barangRepository.findById(id)
					.orElseThrow(() -> new RuntimeException("Barang tidak ditemukan"));

			if (!Boolean.TRUE.equals(barang.getIsactive())) {
				return ResponseUtil.notFound("Barang tidak aktif");
			}

			return ResponseUtil.success("Data barang ditemukan", barang);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.notFound("Barang tidak ditemukan");
		}
	}

	@Override
	public ResponseEntity<Object> findByParam(String column, String value, HttpServletRequest request) {
		try {
			List<Barang> result = switch (column.toLowerCase()) {
				case "kode" -> barangRepository.findByKodeBarangContainingIgnoreCase(value)
						.stream().filter(b -> b.getIsactive()).toList();
				case "nama" -> barangRepository.findByNamaBarangContainingIgnoreCase(value)
						.stream().filter(b -> b.getIsactive()).toList();
				default -> barangRepository.findBarangByIsactiveTrue();
			};

			if (result.isEmpty()) {
				return ResponseUtil.notFound("Data barang tidak ditemukan");
			}

			return ResponseUtil.success("Data barang ditemukan", result);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal mencari barang");
		}
	}
}