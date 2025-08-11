package com.ricky.ricky_rest_api.repository;

import com.ricky.ricky_rest_api.model.OrderStatus;
import com.ricky.ricky_rest_api.model.SalesOrder;
import com.ricky.ricky_rest_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

	SalesOrder findByNoFaktur(String noFaktur);

	// Ambil semua berdasarkan status
	List<SalesOrder> findByStatusOrderByCreatedAtDesc(OrderStatus status);

	// Filter by status AND salesPerson
	List<SalesOrder> findByStatusAndSalesPersonOrderByCreatedAtDesc(OrderStatus status, User salesPerson);

	/**
	 * Temukan SalesOrder dengan lock write (untuk mencegah race condition saat approve)
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@QueryHints({
			@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000") // 3 detik timeout
	})
	Optional<SalesOrder> findById(Long id);

	//search
	@Query("SELECT so FROM SalesOrder so " +
			"JOIN so.customer c " +
			"JOIN so.salesPerson sp " +
			"WHERE so.status = :status " +
			"AND (UPPER(so.noFaktur) LIKE UPPER(CONCAT('%', :query, '%')) " +
			"OR UPPER(c.namaCustomer) LIKE UPPER(CONCAT('%', :query, '%')) " +
			"OR UPPER(sp.fullName) LIKE UPPER(CONCAT('%', :query, '%')))")
	List<SalesOrder> searchByStatusAndQuery(
			@Param("status") OrderStatus status,
			@Param("query") String query
	);
}