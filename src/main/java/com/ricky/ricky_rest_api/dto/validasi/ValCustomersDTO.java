package com.ricky.ricky_rest_api.dto.validasi;

import jakarta.validation.constraints.*;

public class ValCustomersDTO {

	@NotBlank(message = "Kode customer wajib diisi")
	private String kodeCustomer;

	@NotBlank(message = "Nama customer wajib diisi")
	private String namaCustomer;

	private String address;

	@Pattern(regexp = "^\\d{10,15}$", message = "Nomor telepon tidak valid")
	private String phone;

	@Email(message = "Format email tidak valid")
	private String email;

	@NotNull(message = "Status active wajib diisi")
	private Boolean isactive;

	public String getKodeCustomer() {
		return kodeCustomer;
	}

	public void setKodeCustomer(String kodeCustomer) {
		this.kodeCustomer = kodeCustomer;
	}

	public String getNamaCustomer() {
		return namaCustomer;
	}

	public void setNamaCustomer(String namaCustomer) {
		this.namaCustomer = namaCustomer;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
