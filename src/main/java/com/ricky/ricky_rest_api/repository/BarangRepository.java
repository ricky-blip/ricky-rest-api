package com.ricky.ricky_rest_api.repository;

import com.ricky.ricky_rest_api.model.Barang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

import java.util.List;
import java.util.Optional;

@Repository
public interface BarangRepository extends JpaRepository<Barang, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@QueryHints({
			@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000") // 3 detik
	})
	Optional<Barang> findById(Long id);

	List<Barang> findBarangByIsactiveTrue();

	Optional<Barang> findByKodeBarang(String kodeBarang);

	List<Barang> findByKodeBarangContainingIgnoreCase(String kode);

	List<Barang> findByNamaBarangContainingIgnoreCase(String nama);
}