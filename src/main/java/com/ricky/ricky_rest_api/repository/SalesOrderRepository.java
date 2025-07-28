// File: com/ricky/ricky_rest_api/repository/SalesOrderRepository.java
package com.ricky.ricky_rest_api.repository;

import com.ricky.ricky_rest_api.model.SalesOrder;
import com.ricky.ricky_rest_api.model.OrderStatus;
import com.ricky.ricky_rest_api.model.User; // Untuk sales person/manager
import com.ricky.ricky_rest_api.model.Customers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
	Optional<SalesOrder> findByNoFaktur(String noFaktur);
}