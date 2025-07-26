package com.ricky.ricky_rest_api.dto.validasi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ValLoginUserDTO {
	@NotNull(message = "Username Tidak Boleh Kosong")
	@Pattern(regexp = "^[a-z0-9\\.]{8,16}$", message = "Format Username Tidak Valid. Username harus 8-16 karakter, terdiri dari huruf kecil, angka, dan titik.")
	private String username;

	@NotNull(message = "Password Tidak Boleh Kosong")
	@Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[@_#\\-$])[\\w@_#\\-$]{8,15}$",
			message = "Format Password Tidak Valid. Password harus 8-15 karakter, mengandung huruf besar, huruf kecil, angka, dan salah satu dari [@_#\\-$]")
	private String password;

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
