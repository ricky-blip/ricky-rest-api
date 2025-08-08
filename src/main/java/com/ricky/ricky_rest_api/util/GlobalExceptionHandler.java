// File: util/GlobalExceptionHandler.java
package com.ricky.ricky_rest_api.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	// ✅ Handle validasi DTO (@Valid)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Map<String, Object>>> handleValidation(MethodArgumentNotValidException ex) {
		Map<String, Object> errors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});

		ApiResponse<Map<String, Object>> body = new ApiResponse<>(
				400,
				"error",
				"Validasi input gagal",
				errors
		);
		return ResponseEntity.status(400).body(body);
	}

	// ✅ Handle login gagal (username/password salah)
	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiResponse<Object>> handleBadCredentials(BadCredentialsException e) {
		ApiResponse<Object> body = new ApiResponse<>(
				401,
				"error",
				"Username atau password salah",
				null
		);
		return ResponseEntity.status(401).body(body);
	}

	// ✅ Handle autentikasi lain (optional)
	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ApiResponse<Object>> handleAuthException(AuthenticationException e) {
		ApiResponse<Object> body = new ApiResponse<>(
				401,
				"error",
				"Autentikasi gagal: " + e.getMessage(),
				null
		);
		return ResponseEntity.status(401).body(body);
	}

	// ✅ Handle akses ditolak
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException e) {
		ApiResponse<Object> body = new ApiResponse<>(
				403,
				"error",
				"Akses ditolak: Anda tidak memiliki izin",
				null
		);
		return ResponseEntity.status(403).body(body);
	}

	// ✅ Handle error umum
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Object>> handleGeneral(Exception e) {
		ApiResponse<Object> body = new ApiResponse<>(
				500,
				"error",
				"Terjadi kesalahan internal",
				null
		);
		return ResponseEntity.status(500).body(body);
	}
}