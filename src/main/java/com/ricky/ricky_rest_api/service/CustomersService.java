package com.ricky.ricky_rest_api.service;

import com.ricky.ricky_rest_api.core.IService;
import com.ricky.ricky_rest_api.dto.validasi.ValCustomersDTO;
import com.ricky.ricky_rest_api.model.Customers;
import com.ricky.ricky_rest_api.repository.CustomersRepository;
import com.ricky.ricky_rest_api.util.GlobalResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class CustomersService implements IService<ValCustomersDTO> {

	@Autowired
	private CustomersRepository customersRepository;

	private String className = "CustomersService";

	@Override
	public ResponseEntity<Object> save(ValCustomersDTO dto, HttpServletRequest request) {
		if (dto == null) {
			return GlobalResponse.objectNull("TRN01FV001", request);
		}

		try {
			Customers customer = new Customers();
			customer.setKodeCustomer(dto.getKodeCustomer());
			customer.setNamaCustomer(dto.getNamaCustomer());
			customer.setAddress(dto.getAddress());
			customer.setPhone(dto.getPhone());
			customer.setEmail(dto.getEmail());
			customer.setActive(true);
			customersRepository.save(customer);

		} catch (Exception e) {
			return GlobalResponse.internalServerError("TRN01FE001", request);
		}

		return GlobalResponse.dataBerhasilDisimpan(request);
	}

	@Override
	public ResponseEntity<Object> update(Long id, ValCustomersDTO f, HttpServletRequest request) {
		return null;
	}

	@Override
	public ResponseEntity<Object> delete(Long id, HttpServletRequest request) {
		return null;
	}

	@Override
	public ResponseEntity<Object> findAll(Pageable pageable, HttpServletRequest request) {
		return null;
	}

	@Override
	public ResponseEntity<Object> findById(Long id, HttpServletRequest request) {
		return null;
	}

	@Override
	public ResponseEntity<Object> findByParam(Pageable pageable, String column, String value, HttpServletRequest request) {
		return null;
	}
}
