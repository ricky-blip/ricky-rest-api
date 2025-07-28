// File: com/ricky.ricky_rest_api.service.SalesOrderService.java
package com.ricky.ricky_rest_api.service;

import com.ricky.ricky_rest_api.core.IService;
import com.ricky.ricky_rest_api.dto.validasi.ValSalesOrderDTO;
import com.ricky.ricky_rest_api.dto.validasi.ValSalesOrderDetailDTO;
import com.ricky.ricky_rest_api.dto.validasi.ValSalesOrderInputDTO;
import com.ricky.ricky_rest_api.model.*;
import com.ricky.ricky_rest_api.repository.*;
import com.ricky.ricky_rest_api.handler.ResponseHandler; // <-- Import ResponseHandler dari bootcamp
import com.ricky.ricky_rest_api.util.LoggingFile;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class SalesOrderService implements IService<ValSalesOrderInputDTO> {

	@Autowired
	private SalesOrderRepository soRepository;

	private String className = "SalesOrderService";

	@Override
	public ResponseEntity<Object> save(ValSalesOrderInputDTO valSalesOrderInputDTO, HttpServletRequest request) {
		return null;
	}

	@Override
	public ResponseEntity<Object> update(Long id, ValSalesOrderInputDTO valSalesOrderInputDTO, HttpServletRequest request) {
		return null;
	}

	@Override
	public ResponseEntity<Object> delete(Long id, HttpServletRequest request) {
		return null;
	}

	@Override
	public ResponseEntity<Object> findAll(HttpServletRequest request) {
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
