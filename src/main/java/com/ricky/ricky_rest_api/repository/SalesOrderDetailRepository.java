package com.ricky.ricky_rest_api.repository;
import com.ricky.ricky_rest_api.model.SalesOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesOrderDetailRepository extends JpaRepository<SalesOrderDetail, Long> {
	List<SalesOrderDetail> findBySalesOrder_IdSalesOrder(Long idSalesOrder);
}
