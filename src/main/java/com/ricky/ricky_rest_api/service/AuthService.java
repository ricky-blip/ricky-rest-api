// File: AuthService.java (diperbaiki)
package com.ricky.ricky_rest_api.service;

import com.ricky.ricky_rest_api.dto.validasi.ValLoginUserDTO;
import com.ricky.ricky_rest_api.model.User;
import com.ricky.ricky_rest_api.repository.UserRepository;
import com.ricky.ricky_rest_api.security.JwtUtility;
import com.ricky.ricky_rest_api.util.LoggingFile;
import com.ricky.ricky_rest_api.util.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private JwtUtility jwtUtility;

	private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	private static final String ERROR_USER_NOT_FOUND = "AUT00FV021";
	private static final String ERROR_INVALID_CREDENTIALS = "AUT00FV022";
	private static final String ERROR_SERVER = "AUT00FE021";

	public ResponseEntity<Object> login(ValLoginUserDTO loginDto, HttpServletRequest request) {
		Map<String, Object> responseData = new HashMap<>();
		User userFromDb = null;

		try {
			String inputUsername = loginDto.getUsername();
			String inputPassword = loginDto.getPassword();

			if (inputUsername == null || inputUsername.isEmpty() || inputPassword == null || inputPassword.isEmpty()) {
				return ResponseUtil.badRequest("Username dan password wajib diisi");
			}

			Optional<User> optionalUser = userRepository.findByUsernameAndIsActive(inputUsername, true);
			if (optionalUser.isEmpty()) {
				return ResponseUtil.notFound("User tidak ditemukan");
			}

			userFromDb = optionalUser.get();

			if (!passwordEncoder.matches(inputPassword, userFromDb.getPassword())) {
				return ResponseUtil.badRequest("Password salah");
			}

			Map<String, Object> jwtClaims = new HashMap<>();
			jwtClaims.put("id", userFromDb.getIdUser());
			jwtClaims.put("naleng", userFromDb.getFullName());
			jwtClaims.put("role", userFromDb.getRole().name());

			String token = jwtUtility.doGenerateToken(jwtClaims, userFromDb.getUsername());

			Map<String, Object> userData = new HashMap<>();
			userData.put("idUser", userFromDb.getIdUser());
			userData.put("username", userFromDb.getUsername());
			userData.put("fullName", userFromDb.getFullName());
			userData.put("role", userFromDb.getRole().name());

			responseData.put("token", token);
			responseData.put("user", userData);

			return ResponseUtil.success("Login berhasil", responseData);

		} catch (Exception e) {
			LoggingFile.logException("AuthService", "login(...) Input: " + (loginDto != null ? loginDto.getUsername() : "null"), e);
			return ResponseUtil.serverError("Terjadi kesalahan internal");
		}
	}

	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<User> userOptional = userRepository.findByUsername(username);
		if (userOptional.isPresent()) {
			User user = userOptional.get();
			if (user.isEnabled()) {
				return user;
			} else {
				throw new UsernameNotFoundException("User is not active");
			}
		} else {
			throw new UsernameNotFoundException("User not found: " + username);
		}
	}
}