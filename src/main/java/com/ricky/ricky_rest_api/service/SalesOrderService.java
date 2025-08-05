package com.ricky.ricky_rest_api.service;

import com.ricky.ricky_rest_api.core.IService;
import com.ricky.ricky_rest_api.dto.response.DetailBarangDTO;
import com.ricky.ricky_rest_api.dto.response.ResDetailSalesOrderDTO;
import com.ricky.ricky_rest_api.dto.response.ResDraftSalesOrderDTO;
import com.ricky.ricky_rest_api.dto.response.UserDTO;
import com.ricky.ricky_rest_api.dto.validasi.ValSalesOrderDTO;
import com.ricky.ricky_rest_api.dto.validasi.ValSalesOrderDetailDTO;
import com.ricky.ricky_rest_api.model.*;
import com.ricky.ricky_rest_api.repository.*;
import com.ricky.ricky_rest_api.util.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SalesOrderService implements IService<ValSalesOrderDTO> {

	@Autowired
	private SalesOrderRepository salesOrderRepository;

	@Autowired
	private SalesOrderDetailRepository salesOrderDetailRepository;

	@Autowired
	private CustomersRepository customersRepository;

	@Autowired
	private BarangRepository barangRepository;

	@Autowired
	private UserRepository userRepository;

	private final String className = "SalesOrderService";

	@Override
	@Transactional
	public ResponseEntity<Object> save(ValSalesOrderDTO dto, HttpServletRequest request) {
		try {
			// 1. Ambil user dari JWT
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication == null || !authentication.isAuthenticated()) {
				return ResponseUtil.unauthorized("User tidak terautentikasi");
			}
			String username = authentication.getName();

			User currentUser = userRepository.findByUsername(username)
					.orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

			// 2. Validasi Customer
			Customers customer = customersRepository.findById(dto.getIdCustomer())
					.orElseThrow(() -> new RuntimeException("Customer tidak ditemukan"));

			// 3. Buat SalesOrder
			SalesOrder salesOrder = new SalesOrder();
			salesOrder.setNoFaktur(generateFakturNumber());
			salesOrder.setCustomer(customer);
			salesOrder.setSalesPerson(currentUser);
			salesOrder.setSalesManager(
					currentUser.getRole() == Role.SALES_MANAGER ? currentUser : null
			);
			salesOrder.setTransactionType(TransactionType.valueOf(dto.getTransactionType().toUpperCase()));
			salesOrder.setStatus(OrderStatus.PENDING);
			salesOrder.setTanggalOrder(LocalDateTime.now());

			// 4. Proses Detail & Hitung Subtotal
			BigDecimal subtotal = BigDecimal.ZERO;

			for (ValSalesOrderDetailDTO detailDto : dto.getDetails()) {
				Barang barang = barangRepository.findById(detailDto.getIdBarang())
						.orElseThrow(() -> new RuntimeException("Barang tidak ditemukan"));

				SalesOrderDetail detail = new SalesOrderDetail();
				detail.setBarang(barang);
				detail.setAddress(detailDto.getAddress());
				detail.setQuantity(detailDto.getQuantity());
				detail.setHargaJual(detailDto.getHargaJual());
				detail.setSalesOrder(salesOrder);

				subtotal = subtotal.add(detail.getHargaJual().multiply(BigDecimal.valueOf(detail.getQuantity())));
				salesOrder.addDetail(detail);
			}

			// 5. Hitung PPN & Total
			BigDecimal ppnRate = new BigDecimal("10.00"); // 10%
			BigDecimal jumlahPpn = subtotal.multiply(ppnRate.divide(new BigDecimal("100")));
			BigDecimal totalHarga = subtotal.add(jumlahPpn);

			salesOrder.setSubtotal(subtotal);
			salesOrder.setPpn(ppnRate);
			salesOrder.setJumlahPpn(jumlahPpn);
			salesOrder.setTotalHarga(totalHarga);

			// 6. Simpan
			salesOrderRepository.save(salesOrder);

			return ResponseUtil.created("Data Sales Order berhasil disimpan", null);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal menyimpan Sales Order");
		}
	}

	@Override
	public ResponseEntity<Object> update(Long id, ValSalesOrderDTO dto, HttpServletRequest request) {
		return ResponseUtil.methodNotAllowed("Update belum didukung");
	}

	@Override
	public ResponseEntity<Object> delete(Long id, HttpServletRequest request) {
		return ResponseUtil.methodNotAllowed("Delete belum didukung");
	}

	@Override
	public ResponseEntity<Object> findAll(HttpServletRequest request) {
		try {
			List<SalesOrder> drafts = salesOrderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.PENDING);

			List<ResDraftSalesOrderDTO> draftDTOs = drafts.stream().map(draft -> {
				return new ResDraftSalesOrderDTO(
						draft.getIdSalesOrder(),
						draft.getNoFaktur(),
						draft.getCustomer().getNamaCustomer(),
						draft.getTransactionType().name(),
						draft.getTotalHarga()
				);
			}).collect(Collectors.toList());

			return ResponseUtil.success("Data draft Sales Order ditemukan", draftDTOs);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal mengambil data draft");
		}
	}

	@Override
	public ResponseEntity<Object> findById(Long id, HttpServletRequest request) {
		try {
			SalesOrder salesOrder = salesOrderRepository.findById(id)
					.orElseThrow(() -> new RuntimeException("Sales Order tidak ditemukan"));

			if (salesOrder.getStatus() != OrderStatus.PENDING) {
				return ResponseUtil.notFound("Hanya draft (status PENDING) yang bisa dilihat");
			}

			ResDetailSalesOrderDTO dto = new ResDetailSalesOrderDTO();
			dto.setNoFaktur(salesOrder.getNoFaktur());
			dto.setTanggalOrder(salesOrder.getTanggalOrder());
			dto.setTransactionType(salesOrder.getTransactionType().name());
			dto.setStatus(salesOrder.getStatus());

			Customers customer = salesOrder.getCustomer();
			dto.setNamaCustomer(customer.getNamaCustomer());
			dto.setAlamatCustomer(customer.getAddress());
			dto.setPhoneCustomer(customer.getPhone());
			dto.setEmailCustomer(customer.getEmail());

			dto.setSubtotal(salesOrder.getSubtotal());
			dto.setPpn(salesOrder.getPpn());
			dto.setJumlahPpn(salesOrder.getJumlahPpn());
			dto.setTotalHarga(salesOrder.getTotalHarga());

			dto.setSalesPerson(new UserDTO(
					salesOrder.getSalesPerson().getFullName(),
					salesOrder.getSalesPerson().getUsername(),
					salesOrder.getSalesPerson().getRole().name()
			));

			if (salesOrder.getSalesManager() != null) {
				dto.setSalesManager(new UserDTO(
						salesOrder.getSalesManager().getFullName(),
						salesOrder.getSalesManager().getUsername(),
						salesOrder.getSalesManager().getRole().name()
				));
			}

			List<DetailBarangDTO> detailDTOs = salesOrder.getDetails().stream().map(detail -> {
				Barang barang = detail.getBarang();
				return new DetailBarangDTO(
						barang.getKodeBarang(),
						barang.getNamaBarang(),
						barang.getSatuan(),
						detail.getQuantity(),
						detail.getHargaJual(),
						detail.getAddress()
				);
			}).collect(Collectors.toList());

			dto.setDetails(detailDTOs);

			return ResponseUtil.success("Data draft Sales Order ditemukan", dto);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal mengambil detail draft");
		}
	}

	@Override
	public ResponseEntity<Object> findByParam(String column, String value, HttpServletRequest request) {
		return ResponseUtil.notFound("Fitur pencarian belum diimplementasikan");
	}

	private String generateFakturNumber() {
		return "SO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	}
}