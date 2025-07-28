package com.ricky.ricky_rest_api.core;

import com.ricky.ricky_rest_api.dto.validasi.ValCustomersDTO;
import com.ricky.ricky_rest_api.dto.validasi.ValSalesOrderDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

public interface IService<F> {

	public ResponseEntity<Object> save(F f, HttpServletRequest request);//001-010
	public ResponseEntity<Object> update(Long id,F f, HttpServletRequest request);//011-020
	public ResponseEntity<Object> delete(Long id, HttpServletRequest request);//021-030
//	public ResponseEntity<Object> findAll(Pageable pageable,HttpServletRequest request);//031-040
	public ResponseEntity<Object> findAll(HttpServletRequest request);//031-040
	public ResponseEntity<Object> findById(Long id,HttpServletRequest request);//041-050
	public ResponseEntity<Object> findByParam(Pageable pageable,String column, String value,HttpServletRequest request);//051-060
//	ResponseEntity<Object> update(Long id, ValCustomersDTO f, HttpServletRequest request);
}
