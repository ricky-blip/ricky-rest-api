
package com.ricky.ricky_rest_api.controller;

import com.ricky.ricky_rest_api.dto.validasi.ValLoginUserDTO;
import com.ricky.ricky_rest_api.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	private AuthService authService;

	@PostMapping("/login")
	public ResponseEntity<Object> login(
			@Valid @RequestBody ValLoginUserDTO loginDto,
			HttpServletRequest request) {

		return authService.login(loginDto, request);

	}

}
