package com.ricky.ricky_rest_api.security;

import com.ricky.ricky_rest_api.config.JwtConfig;
import com.ricky.ricky_rest_api.core.MyHttpServletRequestWrapper;
import com.ricky.ricky_rest_api.service.AuthService;
import com.ricky.ricky_rest_api.util.LoggingFile;
import com.ricky.ricky_rest_api.util.RequestCapture;
import com.ricky.ricky_rest_api.util.ResponseUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtility jwtUtility;

	@Autowired
	private AuthService authService;

	@Override
	protected void doFilterInternal(HttpServletRequest request,
	                                HttpServletResponse response,
	                                FilterChain filterChain) throws ServletException, IOException {

		String authorization = request.getHeader("Authorization");
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			try {
				filterChain.doFilter(request, response);
			} catch (Exception e) {
				LoggingFile.logException("JwtFilter", "filterChain.doFilter", e);
			}
			return;
		}

		String token = authorization.substring(7); // Ambil token setelah "Bearer "

		// Jika token dienkripsi
		if (JwtConfig.getTokenEncryptEnable().equals("y")) {
			try {
				token = Crypto.performDecrypt(token);
			} catch (Exception e) {
				sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token tidak valid: Gagal dekripsi");
				return;
			}
		}

		String username;
		try {
			username = jwtUtility.getUsernameFromToken(token);
		} catch (IllegalArgumentException e) {
			sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token tidak valid: Tidak bisa ekstrak username");
			return;
		} catch (ExpiredJwtException e) {
			sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token telah kadaluarsa. Silakan login ulang.");
			return;
		} catch (MalformedJwtException e) {
			sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token tidak valid: Format token salah");
			return;
		}
//		catch (SignatureException e) {
//			sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token tidak valid: Signature tidak cocok");
//			return;
//		}
		catch (UnsupportedJwtException e) {
			sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token tidak valid: Format JWT tidak didukung");
			return;
		} catch (Exception e) {
			LoggingFile.logException("JwtFilter", "Error saat parsing token", e);
			sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token tidak valid");
			return;
		}

		// Jika username valid dan belum ada autentikasi
		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			try {
				if (jwtUtility.validateToken(token)) {
					UserDetails userDetails = authService.loadUserByUsername(username);
					UsernamePasswordAuthenticationToken authentication =
							new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
					authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
			} catch (Exception e) {
				LoggingFile.logException("JwtFilter", "Gagal autentikasi user: " + username, e);
				sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Gagal autentikasi user");
				return;
			}
		}

		// Lanjut ke filter berikutnya
		try {
			// Wrap request jika perlu (untuk multipart)
			String contentType = request.getContentType() == null ? "" : request.getContentType();
			if (!contentType.startsWith("multipart/form-data") && !contentType.isEmpty()) {
				request = new MyHttpServletRequestWrapper(request);
			}

			filterChain.doFilter(request, response);
		} catch (Exception e) {
			LoggingFile.logException("JwtFilter", "filterChain.doFilter gagal", e);
			sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Terjadi kesalahan internal");
		}
	}

	/**
	 * Kirim response JSON error dengan format konsisten
	 */
	private void sendError(HttpServletResponse response, int statusCode, String message) {
		try {
			response.setStatus(statusCode);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");

			var errorResponse = ResponseUtil.unauthorized(message).getBody();
			response.getWriter().write(errorResponse.toString());
			response.getWriter().flush();
		} catch (Exception e) {
			LoggingFile.logException("JwtFilter", "Gagal kirim response error", e);
		}
	}
}