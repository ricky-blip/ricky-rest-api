package com.ricky.ricky_rest_api.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ApiResponse<Object>> handleAuthException(AuthenticationException e) {
		ApiResponse<Object> response = new ApiResponse<>(
				401,
				"error",
				"Autentikasi gagal: " + e.getMessage(),
				null
		);
		return ResponseEntity.status(401).body(response);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException e) {
		ApiResponse<Object> response = new ApiResponse<>(
				403,
				"error",
				"Akses ditolak: Anda tidak memiliki izin untuk melakukan aksi ini",
				null
		);
		return ResponseEntity.status(403).body(response);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Object>> handleGeneralException(Exception e) {
		ApiResponse<Object> response = new ApiResponse<>(
				500,
				"error",
				"Terjadi kesalahan internal: " + e.getMessage(),
				null
		);
		return ResponseEntity.status(500).body(response);
	}
}