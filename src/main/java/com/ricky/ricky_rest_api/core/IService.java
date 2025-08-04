package com.ricky.ricky_rest_api.core;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface IService<F> {
	ResponseEntity<Object> save(F f, HttpServletRequest request); // 001-010
	ResponseEntity<Object> update(Long id, F f, HttpServletRequest request); // 011-020
	ResponseEntity<Object> delete(Long id, HttpServletRequest request); // 021-030
	ResponseEntity<Object> findAll(HttpServletRequest request); // 031-040
	ResponseEntity<Object> findById(Long id, HttpServletRequest request); // 041-050
	ResponseEntity<Object> findByParam(String column, String value, HttpServletRequest request); // 051-060
}