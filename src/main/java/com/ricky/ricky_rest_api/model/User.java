package com.ricky.ricky_rest_api.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_user")
	private Long idUser;

	@Column(name = "username", length = 50, nullable = false, unique = true)
	private String username;

	@Column(name = "password", length = 255, nullable = false)
	private String password;

	@Column(name = "full_name", length = 100, nullable = false)
	private String fullName;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, columnDefinition = "ENUM('SALES', 'SALES_MANAGER') DEFAULT 'SALES'")
	private Role role; // Enum Role

	@Column(name = "created_at", updatable = false)
	@CreationTimestamp
	private LocalDateTime createdAt;

	@Column(name = "updated_at", insertable = false)
	@UpdateTimestamp
	private LocalDateTime updatedAt;

	// INI ADALAH BAGIAN PENTING:
	// Kolom di database adalah `isactive` (lowercase semua)
	// Untuk mengikuti konvensi JavaBeans dan memetakan dengan benar,
	// nama field Java biasanya `isActive` (camelCase dengan 'is' prefix untuk boolean)
	// dan getter-nya `getIsActive()`.
	@Column(name = "isactive", nullable = false) // Nama kolom di DB
	private Boolean isActive; // Nama field Java

	@Column(name = "fcm_token")
	private String fcmToken;

	public Boolean getActive() {
		return isActive;
	}

	public void setActive(Boolean active) {
		isActive = active;
	}

	public String getFcmToken() {
		return fcmToken;
	}

	public void setFcmToken(String fcmToken) {
		this.fcmToken = fcmToken;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// Misalnya, role disimpan sebagai enum 'sales' atau 'sales_manager'
		// Kita ubah menjadi 'ROLE_SALES' atau 'ROLE_SALES_MANAGER' untuk Spring Security
		if (role != null) {
			return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name().toUpperCase()));
		}
		return Collections.emptyList(); // Atau lempar exception jika role wajib
	}

	@Override
	public boolean isAccountNonExpired() {
		return true; // Atau implementasi logika bisnis Anda
	}

	@Override
	public boolean isAccountNonLocked() {
		return true; // Atau implementasi logika bisnis Anda
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true; // Atau implementasi logika bisnis Anda
	}

	@Override
	public boolean isEnabled() {
		// User dianggap enabled jika isActive = true
		return Boolean.TRUE.equals(isActive);
	}

	public Long getIdUser() {
		return idUser;
	}

	public void setIdUser(Long idUser) {
		this.idUser = idUser;
	}

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

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean active) {
		isActive = active;
	}
}
