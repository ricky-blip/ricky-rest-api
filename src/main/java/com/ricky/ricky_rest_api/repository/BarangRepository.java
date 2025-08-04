package com.ricky.ricky_rest_api.repository;

import com.ricky.ricky_rest_api.model.Barang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BarangRepository extends JpaRepository<Barang, Long> {
	List<Barang> findBarangByIsactiveTrue();
}
