package com.ricky.ricky_rest_api.service;

import com.ricky.ricky_rest_api.dto.validasi.ValLoginUserDTO;
import com.ricky.ricky_rest_api.model.User;
import com.ricky.ricky_rest_api.repository.UserRepository;
import com.ricky.ricky_rest_api.security.JwtUtility;
import com.ricky.ricky_rest_api.util.GlobalResponse;
import com.ricky.ricky_rest_api.util.LoggingFile;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

	// Inisialisasi BCryptPasswordEncoder untuk membandingkan password
	private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	private static final String ERROR_USER_NOT_FOUND = "AUT00FV021"; // AUTH-00 (Auth Module), FV (Validation Error), 021 (Login specific)
	private static final String ERROR_INVALID_CREDENTIALS = "AUT00FV022";
	private static final String ERROR_SERVER = "AUT00FE021"; // FE (Fatal/Error)

	public ResponseEntity<Object> login(ValLoginUserDTO loginDto, HttpServletRequest request) {
		Map<String, Object> responseData = new HashMap<>(); // Map untuk menyimpan data response akhir
		User userFromDb = null; // Untuk menyimpan objek User yang ditemukan di DB

		try {
			// 1. Ambil username dan password dari DTO
			String inputUsername = loginDto.getUsername();
			String inputPassword = loginDto.getPassword();

			// 2. Validasi input dasar (meskipun sebagian besar sudah dilakukan oleh @Valid di controller)
			//    Kita bisa mengandalkan @Valid dan @NotNull/@NotBlank di DTO untuk ini.
			if (inputUsername == null || inputUsername.isEmpty() || inputPassword == null || inputPassword.isEmpty()) {
				// Menggunakan method helper dari GlobalResponse yang sesuai
				// Anda bisa buat method objectNull khusus untuk ini jika belum ada
				// atau gunakan yang sudah ada dan sesuaikan pesannya.
				return GlobalResponse.objectNull(ERROR_USER_NOT_FOUND, request);
				// Atau bisa juga: return GlobalResponse.dataTidakDitemukan(ERROR_USER_NOT_FOUND, request);
				// Tergantung konteks error code yang diinginkan dan pesan yang diinginkan.
			}

			// 3. Cari user di database berdasarkan username dan status aktif
			// *** CATATAN PENTING ***
			// Pastikan method findByUsernameAndActive ada di UserRepository Anda.
			// Jika DTO mendukung email/noHp seperti di bootcamp, gunakan method seperti findByUsernameOrEmailOrPhoneAndActive.
			Optional<User> optionalUser = userRepository.findByUsernameAndIsActive(inputUsername, true);

			// 4. Jika user tidak ditemukan atau tidak aktif
			if (optionalUser.isEmpty()) {
				// Logging bisa ditambahkan di sini jika perlu
				// Menggunakan method helper dari GlobalResponse untuk error spesifik login
				// Gunakan pesan umum untuk keamanan
				return GlobalResponse.dataTidakDitemukan(ERROR_USER_NOT_FOUND, request); // Sesuaikan jika ada method khusus
				// Atau buat method baru di GlobalResponse jika perlu pesan khusus
				// return GlobalResponse.invalidCredentials(ERROR_USER_NOT_FOUND, request);
			}

			// 5. Jika user ditemukan, ambil objek User
			userFromDb = optionalUser.get();

			// 6. Bandingkan password yang diinput dengan password yang di-hash di database
			// Asumsi: Password di database sudah di-hash saat registrasi/user dibuat
			// Di bootcamp: String pwdDB = userFromDb.getUsername()+inputPassword;
			//              if(!BcryptImpl.verifyHash(pwdDB,userFromDb.getPassword())) { ... }
			// Kita gunakan BCryptPasswordEncoder yang lebih standar:
			if (!passwordEncoder.matches(inputPassword, userFromDb.getPassword())) {
				// Logging bisa ditambahkan di sini jika perlu
				// Menggunakan method helper dari GlobalResponse untuk error spesifik login
				// Gunakan pesan umum untuk keamanan
				return GlobalResponse.dataTidakDitemukan(ERROR_INVALID_CREDENTIALS, request); // Sesuaikan jika ada method khusus
				// Atau: return GlobalResponse.invalidCredentials(ERROR_INVALID_CREDENTIALS, request);
			}

			// 7. Jika password cocok, buat payload untuk JWT
			// Sesuaikan dengan field yang ada di model User Anda
			Map<String, Object> jwtClaims = new HashMap<>();
			// Di bootcamp: mapData.put("em",userNext.getEmail()); // Tidak ada di model User Anda
			jwtClaims.put("id", userFromDb.getIdUser()); // Di bootcamp: userNext.getId()
			// Di bootcamp: mapData.put("hp",userNext.getNoHp()); // Tidak ada di model User Anda
			jwtClaims.put("naleng", userFromDb.getFullName()); // Di bootcamp: userNext.getNamaLengkap()
			// Di bootcamp: List<MenuLoginDTO> menu = mapToMenuLoginDTO(userNext.getAkses().getListMenu());
			//              Tidak ada relasi Akses/Menu di model User Anda, jadi tidak disertakan.
			// Tambahkan role jika perlu di claims
			if (userFromDb.getRole() != null) {
				jwtClaims.put("role", userFromDb.getRole().name()); // Simpan nama enum role
			} else {
				jwtClaims.put("role", "UNKNOWN");
			}

			// 8. Generate JWT Token
			// Asumsi: JwtUtility memiliki method doGenerateToken(Map<String, Object> claims, String subject)
			// subject biasanya username
			String token = jwtUtility.doGenerateToken(jwtClaims, userFromDb.getUsername());

			// 9. Siapkan data user untuk response (tanpa password)
			// Sesuaikan dengan field yang ada di model User Anda
			Map<String, Object> userData = new HashMap<>();
			userData.put("idUser", userFromDb.getIdUser()); // Di bootcamp: userNext.getId()
			userData.put("username", userFromDb.getUsername());
			userData.put("fullName", userFromDb.getFullName()); // Di bootcamp: userNext.getNamaLengkap()
			// Di bootcamp: userNext.getNoHp(), userNext.getEmail() - Tidak ada
			// Di bootcamp: userNext.getLinkImage() - Tidak ada
			// Tambahkan role jika perlu di response
			if (userFromDb.getRole() != null) {
				userData.put("role", userFromDb.getRole());
			}
			// Di bootcamp: List<MenuLoginDTO> menu = ... - Tidak ada

			// 10. Masukkan token dan data user ke response map akhir
			responseData.put("token", token);
			responseData.put("user", userData);
			// Di bootcamp: m.put("menu", menu); - Tidak ada
			// Di bootcamp: m.put("urlImage", userNext.getLinkImage()); - Tidak ada
			// Di bootcamp: if(JwtConfig.getTokenEncryptEnable().equals("y")){ token = Crypto.performEncrypt(token); } - Tidak ada

			// 11. Kembalikan response sukses menggunakan method helper dari GlobalResponse
			// Di bootcamp: return new ResponseHandler().handleResponse("Login Berhasil !!",HttpStatus.OK,m,null,request);
			// Kita sesuaikan dengan GlobalResponse Anda:
			return GlobalResponse.dataDitemukan(responseData, request); // Atau buat method loginSuccess jika belum ada
			// Jika GlobalResponse.dataDitemukan tidak cocok untuk konteks login, buat method baru seperti sebelumnya:
			// return GlobalResponse.loginSuccess(responseData, request);


		} catch (Exception e) {
			// 12. Tangani error yang tidak terduga
			// Pastikan LoggingFile.logException bisa menerima parameter ini atau sesuaikan
			LoggingFile.logException(
					"AuthService",
					"login(ValLoginUserDTO loginDto, HttpServletRequest request). Input: " + (loginDto != null ? loginDto.getUsername() : "null DTO"),
					e
			);
			// Menggunakan method helper dari GlobalResponse untuk error server
			// Di bootcamp: return new ResponseHandler().handleResponse("Terjadi Kesalahan Pada Server",HttpStatus.INTERNAL_SERVER_ERROR,null, "TRN00FE021",request);
			// Kita sesuaikan:
			return GlobalResponse.internalServerError(ERROR_SERVER, request);
		}
	}

	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// Gunakan method repository yang benar dan sudah diperbaiki
		Optional<User> userOptional = userRepository.findByUsername(username);
		if (userOptional.isPresent()) {
			User user = userOptional.get();
			// isEnabled() dari UserDetails akan memeriksa isActive
			if (user.isEnabled()) {
				return user; // Karena User sekarang implements UserDetails
			} else {
				throw new UsernameNotFoundException("User is not active");
			}
		} else {
			throw new UsernameNotFoundException("User not found with username: " + username);
		}
	}
}
