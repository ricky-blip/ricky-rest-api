package com.ricky.ricky_rest_api.security;

import com.ricky.ricky_rest_api.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

	@Autowired
	private JwtFilter jwtFilter;

//	@Autowired
//	@Qualifier("customAuthenticationEntryPoint")
//	private AuthenticationEntryPoint authenticationEntryPoint;
//
//	@Autowired
//	private AuthService authService;

    /*
        401 -> Otentikasi
        403 -> Forbiden / Otorisasi
     */

//	@Bean
//	public AuthenticationProvider authenticationProvider() {
//		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
//		authProvider.setUserDetailsService(authService);
//		return authProvider;
//	}

//	@Bean
//	public SecurityFilterChain securityFilterChain(HttpSecurity http,JwtFilter filter) throws Exception {
//		http.
//				csrf(AbstractHttpConfigurer::disable).
//				authorizeHttpRequests(
//						request->request.requestMatchers(
//								"/auth/**",
//								"/supplier/**",
//								"/contoh/**",
//								"/swagger-ui/**",
//								"/v3/api-docs/**"
//						).permitAll().anyRequest().authenticated()).
//				httpBasic(basic -> basic.authenticationEntryPoint(authenticationEntryPoint)).
//				exceptionHandling(Customizer.withDefaults()).
//				sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).
////				authenticationProvider(authenticationProvider()).
//				addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
//
//		return http.build();
//	}

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
	http
			// 1. Nonaktifkan CSRF karena API stateless JWT
			.csrf(AbstractHttpConfigurer::disable)

			// 2. Konfigurasi otorisasi permintaan
//			.authorizeHttpRequests(requests -> requests
//					// Izinkan akses tanpa autentikasi ke endpoint berikut
//					.requestMatchers(
//							"/api/auth/**",      // Endpoint autentikasi (login, register jika ada)
//							"/supplier/**",      // Izinkan sementara, sesuaikan dengan kebutuhan
//							"/contoh/**",        // Izinkan sementara, sesuaikan dengan kebutuhan
//							"/swagger-ui/**",    // Swagger UI
//							"/v3/api-docs/**"    // OpenAPI docs
//					).permitAll()
//					// Semua request lainnya memerlukan autentikasi
//					.anyRequest().authenticated()
//			)

			.authorizeHttpRequests(requests -> requests
					.requestMatchers(
							"/",
							"/api/auth/**",      // endpoint login
							"/supplier/**",
							"/contoh/**",
							"/swagger-ui/**",    // folder swagger
							"/swagger-ui.html",  // halaman utama swagger
							"/v3/api-docs/**"    // endpoint dokumentasi
					).permitAll()
					.anyRequest().authenticated()
			)


			// 3. Penanganan exception
			// Karena kita menggunakan JWT Filter, penanganan 401 biasanya dilakukan
			// oleh filter itu sendiri atau secara default oleh Spring Security.
			// Jika Anda ingin entry point kustom, buat bean-nya terlebih dahulu.
			// .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint)) // DIHAPUS
			.exceptionHandling(Customizer.withDefaults()) // Gunakan penanganan default

			// 4. Manajemen sesi
			// Karena menggunakan JWT stateless, nonaktifkan pembuatan sesi HTTP
			.sessionManagement(session -> session
					.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)

			// 5. Tambahkan filter JWT Anda sebelum filter autentikasi bawaan Spring
			// Pastikan field jwtFilter di-inject dengan benar (@Autowired di atas)
			.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

	// Jika menggunakan AuthenticationProvider kustom, uncomment baris berikut:
	// .authenticationProvider(authenticationProvider());

	return http.build();
}
}
