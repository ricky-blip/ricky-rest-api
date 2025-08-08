// File: util/ResponseUtil.java
package com.ricky.ricky_rest_api.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

public class ResponseUtil {

	public static <T> ResponseEntity<Object> success(String message, T data) {
		ApiResponse<T> body = new ApiResponse<>(200, "success", message, data);
		return ResponseEntity.ok(body);
	}

	public static <T> ResponseEntity<Object> created(String message, T data) {
		ApiResponse<T> body = new ApiResponse<>(201, "success", message, data);
		return ResponseEntity.status(201).body(body);
	}

	public static <T> ResponseEntity<Object> notFound(String message) {
		ApiResponse<T> body = new ApiResponse<>(404, "error", message, null);
		return ResponseEntity.status(404).body(body);
	}

	public static <T> ResponseEntity<Object> badRequest(String message) {
		ApiResponse<T> body = new ApiResponse<>(400, "error", message, null);
		return ResponseEntity.status(400).body(body);
	}

	public static <T> ResponseEntity<Object> unauthorized(String message) {
		ApiResponse<T> body = new ApiResponse<>(401, "error", message, null);
		return ResponseEntity.status(401).body(body);
	}

	public static <T> ResponseEntity<Object> forbidden(String message) {
		ApiResponse<T> body = new ApiResponse<>(403, "error", message, null);
		return ResponseEntity.status(403).body(body);
	}

	public static <T> ResponseEntity<Object> serverError(String message) {
		ApiResponse<T> body = new ApiResponse<>(500, "error", message, null);
		return ResponseEntity.status(500).body(body);
	}

	public static <T> ResponseEntity<Object> methodNotAllowed(String message) {
		ApiResponse<T> body = new ApiResponse<>(405, "error", message, null);
		return ResponseEntity.status(405).body(body);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
		List<String> errors = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(x -> x.getDefaultMessage())
				.collect(Collectors.toList());

		ApiResponse<Object> body = new ApiResponse<>(400, "error", "Validasi input gagal", errors);
		return ResponseEntity.status(400).body(body);
	}
}