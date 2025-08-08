package com.ricky.ricky_rest_api.dto.validasi;

import jakarta.validation.constraints.*;

public class ValLoginUserDTO {
	@NotBlank(message = "Username tidak boleh kosong")
	@Size(min = 8, max = 16, message = "Username harus berisi 8-16 karakter")
	@Pattern(regexp = "^[a-z0-9\\.]{8,16}$", message = "Format Username Tidak Valid. Username harus terdiri dari huruf kecil, angka, dan titik.")
	private String username;

	@NotBlank(message = "Password tidak boleh kosong")
	@Size(min = 8, max = 15, message = "Password harus berisi 8-15 karakter")
	@Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[@_#\\-$])[\\w@_#\\-$]{8,15}$",
			message = "Format Password Tidak Valid. Password harus mengandung huruf besar, huruf kecil, angka, dan simbol.")
	private String password;

	// Getter & Setter
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}