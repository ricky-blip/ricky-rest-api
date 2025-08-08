package com.ricky.ricky_rest_api.service;

import com.ricky.ricky_rest_api.dto.validasi.ChangePasswordDTO;
import com.ricky.ricky_rest_api.dto.validasi.ValLoginUserDTO;
import com.ricky.ricky_rest_api.model.User;
import com.ricky.ricky_rest_api.repository.UserRepository;
import com.ricky.ricky_rest_api.security.JwtUtility;
import com.ricky.ricky_rest_api.util.LoggingFile;
import com.ricky.ricky_rest_api.util.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

		try {
			Optional<User> optionalUser = userRepository.findByUsernameAndIsActive(loginDto.getUsername(), true);
			if (optionalUser.isEmpty()) {
				// Jangan kasih tahu apakah user ada
				return ResponseUtil.unauthorized("Username atau password salah");
			}

			User user = optionalUser.get();

			if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
				// Jangan kasih tahu hanya password salah
				return ResponseUtil.unauthorized("Username atau password salah");
			}

			// Generate token
			Map<String, Object> jwtClaims = new HashMap<>();
			jwtClaims.put("id", user.getIdUser());
			jwtClaims.put("naleng", user.getFullName());
			jwtClaims.put("role", user.getRole().name());

			String token = jwtUtility.doGenerateToken(jwtClaims, user.getUsername());

			Map<String, Object> userData = new HashMap<>();
			userData.put("idUser", user.getIdUser());
			userData.put("username", user.getUsername());
			userData.put("fullName", user.getFullName());
			userData.put("role", user.getRole().name());

			responseData.put("token", token);
			responseData.put("user", userData);

			return ResponseUtil.success("Login berhasil", responseData);

		} catch (Exception e) {
			LoggingFile.logException("AuthService", "login(...) Input: " + loginDto.getUsername(), e);
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

	@Transactional
	public ResponseEntity<Object> changePassword(ChangePasswordDTO dto, HttpServletRequest request) {
		try {
			// 1. Ambil user dari JWT
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User user = userRepository.findByUsername(authentication.getName())
					.orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

			// 2. Cek password lama
			if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
				return ResponseUtil.badRequest("Password lama salah");
			}

			// 3. Cek apakah password baru sama dengan password lama
			if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
				return ResponseUtil.badRequest("Password baru tidak boleh sama dengan password lama");
			}

			// 4. Cek konfirmasi password
			if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
				return ResponseUtil.badRequest("Konfirmasi password baru tidak cocok");
			}

			// 5. Update password baru
			user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
			userRepository.save(user);

			return ResponseUtil.success("Password berhasil diubah", null);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal mengubah password");
		}
	}
}