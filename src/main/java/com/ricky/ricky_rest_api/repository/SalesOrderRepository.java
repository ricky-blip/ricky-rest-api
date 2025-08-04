package com.ricky.ricky_rest_api.repository;

import com.ricky.ricky_rest_api.model.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
	SalesOrder findByNoFaktur(String noFaktur);
}