
package com.ricky.ricky_rest_api.controller;

import com.ricky.ricky_rest_api.dto.validasi.ChangePasswordDTO;
import com.ricky.ricky_rest_api.dto.validasi.ValLoginUserDTO;
import com.ricky.ricky_rest_api.model.User;
import com.ricky.ricky_rest_api.repository.UserRepository;
import com.ricky.ricky_rest_api.service.AuthService;
import com.ricky.ricky_rest_api.util.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	private AuthService authService;

	@Autowired
	private UserRepository userRepository;

	@PostMapping("/login")
	public ResponseEntity<Object> login(
			@Valid @RequestBody ValLoginUserDTO loginDto,
			HttpServletRequest request) {

		return authService.login(loginDto, request);

	}

	@PutMapping("/change-password")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Object> changePassword(
			@Valid @RequestBody ChangePasswordDTO dto,
			HttpServletRequest request) {
		return authService.changePassword(dto, request);
	}

	/**
	 * Simpan FCM Token dari Flutter ke User
	 */
	@PostMapping("/save-fcm-token")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Object> saveFcmToken(@RequestBody Map<String, String> request) {
		try {
			String fcmToken = request.get("fcmToken");
			if (fcmToken == null || fcmToken.isEmpty()) {
				return ResponseUtil.badRequest("FCM token tidak boleh kosong");
			}

			// Ambil user dari JWT
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User user = userRepository.findByUsername(authentication.getName())
					.orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

			// Simpan token
			user.setFcmToken(fcmToken);
			userRepository.save(user);

			return ResponseUtil.success("FCM token berhasil disimpan", null);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal menyimpan FCM token");
		}
	}

}
