package com.ricky.ricky_rest_api.service;

import com.ricky.ricky_rest_api.core.IService;
import com.ricky.ricky_rest_api.dto.validasi.ValCustomersDTO;
import com.ricky.ricky_rest_api.model.Customers;
import com.ricky.ricky_rest_api.repository.CustomersRepository;
import com.ricky.ricky_rest_api.util.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomersService implements IService<ValCustomersDTO> {

	@Autowired
	private CustomersRepository customersRepository;

	private final String className = "CustomersService";

	@Override
	public ResponseEntity<Object> save(ValCustomersDTO dto, HttpServletRequest request) {
		if (dto == null) {
			return ResponseUtil.badRequest("Data customer tidak boleh kosong");
		}

		try {
			// Validasi: kodeCustomer harus unik
			if (customersRepository.findByKodeCustomer(dto.getKodeCustomer()).isPresent()) {
				return ResponseUtil.badRequest("Kode customer sudah ada");
			}

			Customers customer = new Customers();
			customer.setKodeCustomer(dto.getKodeCustomer());
			customer.setNamaCustomer(dto.getNamaCustomer());
			customer.setAddress(dto.getAddress());
			customer.setPhone(dto.getPhone());
			customer.setEmail(dto.getEmail());
			customer.setIsactive(true);

			customersRepository.save(customer);

			return ResponseUtil.created("Data customer berhasil disimpan", null);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal menyimpan customer");
		}
	}

	@Override
	public ResponseEntity<Object> update(Long id, ValCustomersDTO dto, HttpServletRequest request) {
		try {
			Customers customer = customersRepository.findById(id)
					.orElseThrow(() -> new RuntimeException("Customer tidak ditemukan"));

			// Cek unik kodeCustomer (kecuali milik dirinya sendiri)
			if (!customer.getKodeCustomer().equals(dto.getKodeCustomer())) {
				if (customersRepository.findByKodeCustomer(dto.getKodeCustomer()).isPresent()) {
					return ResponseUtil.badRequest("Kode customer sudah digunakan");
				}
			}

			customer.setKodeCustomer(dto.getKodeCustomer());
			customer.setNamaCustomer(dto.getNamaCustomer());
			customer.setAddress(dto.getAddress());
			customer.setPhone(dto.getPhone());
			customer.setEmail(dto.getEmail());

			customersRepository.save(customer);

			return ResponseUtil.success("Data customer berhasil diupdate", null);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal mengupdate customer");
		}
	}

	@Override
	public ResponseEntity<Object> delete(Long id, HttpServletRequest request) {
		try {
			Customers customer = customersRepository.findById(id)
					.orElseThrow(() -> new RuntimeException("Customer tidak ditemukan"));

			customer.setIsactive(false); // Soft delete
			customersRepository.save(customer);

			return ResponseUtil.success("Data customer berhasil dihapus", null);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal menghapus customer");
		}
	}

	@Override
	public ResponseEntity<Object> findAll(HttpServletRequest request) {
		try {
			List<Customers> activeCustomers = customersRepository.findCustomerByIsactiveTrue();
			return ResponseUtil.success("Data customer ditemukan", activeCustomers);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal mengambil data customer");
		}
	}

	@Override
	public ResponseEntity<Object> findById(Long id, HttpServletRequest request) {
		try {
			Customers customer = customersRepository.findById(id)
					.orElseThrow(() -> new RuntimeException("Customer tidak ditemukan"));

			if (!Boolean.TRUE.equals(customer.getIsactive())) {
				return ResponseUtil.notFound("Customer tidak aktif");
			}

			return ResponseUtil.success("Data customer ditemukan", customer);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.notFound("Customer tidak ditemukan");
		}
	}

	@Override
	public ResponseEntity<Object> findByParam(String column, String value, HttpServletRequest request) {
		try {
			List<Customers> result = switch (column.toLowerCase()) {
				case "kode" -> customersRepository.findByKodeCustomerContainingIgnoreCase(value)
						.stream().filter(c -> c.getIsactive()).toList();
				case "nama" -> customersRepository.findByNamaCustomerContainingIgnoreCase(value)
						.stream().filter(c -> c.getIsactive()).toList();
				default -> customersRepository.findCustomerByIsactiveTrue();
			};

			if (result.isEmpty()) {
				return ResponseUtil.notFound("Data customer tidak ditemukan");
			}

			return ResponseUtil.success("Data customer ditemukan", result);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal mencari customer");
		}
	}
}