package com.ricky.ricky_rest_api.repository;

import com.ricky.ricky_rest_api.model.Role;
import com.ricky.ricky_rest_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByUsername(String username);

	Optional<User> findByUsernameAndIsActive(String username, Boolean isActive);
	List<User> findByRole(Role role);
	List<User> findByRoleAndIsActive(Role role, Boolean isActive);
	List<User> findByIsActiveTrue();
}
