package com.ricky.ricky_rest_api.dto.response;

public class UserDTO {
	private String fullName;
	private String username;
	private String role;

	public UserDTO() {}

	public UserDTO(String fullName, String username, String role) {
		this.fullName = fullName;
		this.username = username;
		this.role = role;
	}

	public String getFullName() { return fullName; }
	public void setFullName(String fullName) { this.fullName = fullName; }

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }

	public String getRole() { return role; }
	public void setRole(String role) { this.role = role; }
}