package com.ricky.ricky_rest_api.repository;

import com.ricky.ricky_rest_api.model.Customers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomersRepository extends JpaRepository<Customers, Integer> {
	Optional<Customers> findByKodeCustomer(String kodeCustomer);
}
