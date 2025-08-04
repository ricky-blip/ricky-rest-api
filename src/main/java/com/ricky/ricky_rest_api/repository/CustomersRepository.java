package com.ricky.ricky_rest_api.repository;

import com.ricky.ricky_rest_api.model.Barang;
import com.ricky.ricky_rest_api.model.Customers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomersRepository extends JpaRepository<Customers, Long> {
	List<Customers> findCustomerByIsactiveTrue();
	Optional<Customers> findByKodeCustomer(String kodeCustomer);
	List<Customers> findByKodeCustomerContainingIgnoreCase(String kode);
	List<Customers> findByNamaCustomerContainingIgnoreCase(String nama);
}
