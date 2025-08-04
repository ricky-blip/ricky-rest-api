package com.ricky.ricky_rest_api.service;

import com.ricky.ricky_rest_api.core.IService;
import com.ricky.ricky_rest_api.dto.validasi.ValBarangDTO;
import com.ricky.ricky_rest_api.dto.validasi.ValCustomersDTO;
import com.ricky.ricky_rest_api.model.Barang;
import com.ricky.ricky_rest_api.repository.BarangRepository;
import com.ricky.ricky_rest_api.util.GlobalResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BarangService implements IService<ValBarangDTO> {
	
	@Autowired
	private BarangRepository barangRepository;

	private String className = "BarangService";

	@Override
	public ResponseEntity<Object> save(ValBarangDTO dto, HttpServletRequest request) {
		if (dto == null) {
			return GlobalResponse.objectNull("TRN01FV001", request);
		}

		try {
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

			return GlobalResponse.dataBerhasilDisimpan(request);

		} catch (Exception e) {
			return GlobalResponse.internalServerError("TRN01FE001", request);
		}

	}

	@Override
	public ResponseEntity<Object> update(Long id, ValBarangDTO f, HttpServletRequest request) {
		return null;
	}

	@Override
	public ResponseEntity<Object> delete(Long id, HttpServletRequest request) {
		return null;
	}

	@Override
	public ResponseEntity<Object> findAll(HttpServletRequest request) {
		List<Barang> activeBarang = barangRepository.findBarangByIsactiveTrue();
		return ResponseEntity.ok(activeBarang);
	}

	@Override
	public ResponseEntity<Object> findById(Long id, HttpServletRequest request) {
		return null;
	}

	@Override
	public ResponseEntity<Object> findByParam(String column, String value, HttpServletRequest request) {
		return null;
	}
}
