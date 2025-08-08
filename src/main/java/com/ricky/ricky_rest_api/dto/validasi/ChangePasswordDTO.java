package com.ricky.ricky_rest_api.dto.validasi;
import jakarta.validation.constraints.*;

public class ChangePasswordDTO {

	@NotBlank(message = "Password lama wajib diisi")
	private String oldPassword;

	@NotBlank(message = "Password baru wajib diisi")
	@Size(min = 8, max = 15, message = "Password baru harus 8-15 karakter")
	@Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[@_#\\-$])[\\w@_#\\-$]{8,15}$",
			message = "Password baru harus mengandung huruf besar, huruf kecil, angka, dan simbol (@, _, #, \\, -, $)")
	private String newPassword;

	@NotBlank(message = "Konfirmasi password baru wajib diisi")
	private String confirmNewPassword;

	public String getOldPassword() { return oldPassword; }
	public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

	public String getNewPassword() { return newPassword; }
	public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

	public String getConfirmNewPassword() { return confirmNewPassword; }
	public void setConfirmNewPassword(String confirmNewPassword) { this.confirmNewPassword = confirmNewPassword; }
}