package com.ricky.ricky_rest_api.service;

import com.ricky.ricky_rest_api.core.IService;
import com.ricky.ricky_rest_api.dto.response.*;
import com.ricky.ricky_rest_api.dto.validasi.ValSalesOrderDTO;
import com.ricky.ricky_rest_api.dto.validasi.ValSalesOrderDetailDTO;
import com.ricky.ricky_rest_api.dto.validasi.ValSalesOrderDetailEditDTO;
import com.ricky.ricky_rest_api.dto.validasi.ValSalesOrderEditDTO;
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
			return ResponseUtil.created("Sales Order berhasil dibuat", null);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal menyimpan Sales Order");
		}
	}

	@Transactional
	public ResponseEntity<Object> submitToApproval(Long id, HttpServletRequest request) {
		try {
			SalesOrder salesOrder = salesOrderRepository.findById(id)
					.orElseThrow(() -> new RuntimeException("Sales Order tidak ditemukan"));

			// Hanya draft (PENDING) yang bisa dikirim ke approval
			if (salesOrder.getStatus() != OrderStatus.PENDING) {
				return ResponseUtil.badRequest("Hanya Sales Order dengan status PENDING yang bisa dikirim ke approval");
			}

			// Ubah status ke UNVALIDATED
			salesOrder.setStatus(OrderStatus.UNVALIDATED);
			salesOrderRepository.save(salesOrder);

			return ResponseUtil.success("Sales Order berhasil dikirim ke approval", null);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal mengirim ke approval");
		}
	}

	@Override
	@Transactional
	public ResponseEntity<Object> update(Long id, ValSalesOrderDTO dto, HttpServletRequest request) {
		return ResponseUtil.methodNotAllowed("Update belum didukung");
//		try {
//			if (dto == null || dto.getDetails() == null || dto.getDetails().isEmpty()) {
//				return ResponseUtil.badRequest("Data edit Sales Order tidak valid");
//			}
//
//			SalesOrder salesOrder = salesOrderRepository.findById(id)
//					.orElseThrow(() -> new RuntimeException("Sales Order tidak ditemukan"));
//
//			if (!salesOrder.getStatus().equals(OrderStatus.PENDING)) {
//				return ResponseUtil.badRequest("Hanya Sales Order dengan status PENDING yang bisa diedit");
//			}
//
//			// Hapus semua detail lama
//			salesOrder.getDetails().clear();
//
//			// Tambahkan detail baru
//			for (ValSalesOrderDetailEditDTO detailDto : dto.getDetails()) {
//				Barang barang = barangRepository.findById(detailDto.getIdBarang())
//						.orElseThrow(() -> new RuntimeException("Barang tidak ditemukan"));
//
//				SalesOrderDetail detail = new SalesOrderDetail();
//				detail.setBarang(barang);
//				detail.setQuantity(detailDto.getQuantity());
//				detail.setHargaJual(detailDto.getHargaJual());
//				detail.setSalesOrder(salesOrder);
//
//				salesOrder.addDetail(detail);
//			}
//
//			// Hitung ulang subtotal, PPN, dan total
//			BigDecimal subtotal = salesOrder.getDetails().stream()
//					.map(d -> d.getHargaJual().multiply(BigDecimal.valueOf(d.getQuantity())))
//					.reduce(BigDecimal.ZERO, BigDecimal::add);
//
//			BigDecimal ppnRate = new BigDecimal("10.00");
//			BigDecimal jumlahPpn = subtotal.multiply(ppnRate.divide(new BigDecimal("100")));
//			BigDecimal totalHarga = subtotal.add(jumlahPpn);
//
//			salesOrder.setSubtotal(subtotal);
//			salesOrder.setPpn(ppnRate);
//			salesOrder.setJumlahPpn(jumlahPpn);
//			salesOrder.setTotalHarga(totalHarga);
//
//			salesOrderRepository.save(salesOrder);
//
//			return ResponseUtil.success("Sales Order berhasil diperbarui", null);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			return ResponseUtil.serverError("Gagal memperbarui Sales Order");
//		}
	}

	@Transactional
	public ResponseEntity<Object> editSalesOrder(Long id, ValSalesOrderEditDTO dto, HttpServletRequest request) {
		try {
			if (dto == null || dto.getDetails() == null || dto.getDetails().isEmpty()) {
				return ResponseUtil.badRequest("Data edit Sales Order tidak valid");
			}

			SalesOrder salesOrder = salesOrderRepository.findById(id)
					.orElseThrow(() -> new RuntimeException("Sales Order tidak ditemukan"));

			if (!salesOrder.getStatus().equals(OrderStatus.PENDING)) {
				return ResponseUtil.badRequest("Hanya Sales Order dengan status PENDING yang bisa diedit");
			}

			salesOrder.getDetails().clear();

			for (ValSalesOrderDetailEditDTO detailDto : dto.getDetails()) {
				Barang barang = barangRepository.findById(detailDto.getIdBarang())
						.orElseThrow(() -> new RuntimeException("Barang tidak ditemukan"));

				SalesOrderDetail detail = new SalesOrderDetail();
				detail.setBarang(barang);
				detail.setQuantity(detailDto.getQuantity());
				detail.setHargaJual(detailDto.getHargaJual());
				detail.setSalesOrder(salesOrder);

				salesOrder.addDetail(detail);
			}

			// Hitung ulang
			BigDecimal subtotal = salesOrder.getDetails().stream()
					.map(d -> d.getHargaJual().multiply(BigDecimal.valueOf(d.getQuantity())))
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			BigDecimal ppnRate = new BigDecimal("10.00");
			BigDecimal jumlahPpn = subtotal.multiply(ppnRate.divide(new BigDecimal("100")));
			BigDecimal totalHarga = subtotal.add(jumlahPpn);

			salesOrder.setSubtotal(subtotal);
			salesOrder.setPpn(ppnRate);
			salesOrder.setJumlahPpn(jumlahPpn);
			salesOrder.setTotalHarga(totalHarga);

			salesOrderRepository.save(salesOrder);

			return ResponseUtil.success("Sales Order berhasil diperbarui", null);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal memperbarui Sales Order");
		}
	}

	@Override
	@Transactional
	public ResponseEntity<Object> delete(Long id, HttpServletRequest request) {
		try {
			SalesOrder salesOrder = salesOrderRepository.findById(id)
					.orElseThrow(() -> new RuntimeException("Sales Order tidak ditemukan"));

			if (salesOrder.getStatus() != OrderStatus.PENDING) {
				return ResponseUtil.badRequest("Hanya Sales Order dengan status PENDING yang bisa dihapus");
			}

			// Hard delete: hapus dari database
			salesOrderRepository.delete(salesOrder);

			return ResponseUtil.success("Sales Order berhasil dihapus", null);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal menghapus Sales Order");
		}
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

	// File: service/SalesOrderService.java

	public ResponseEntity<Object> findAllUnvalidated(HttpServletRequest request) {
		try {
			List<SalesOrder> unvalidatedOrders = salesOrderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.UNVALIDATED);

			List<ResUnvalidatedSalesOrderDTO> dtoList = unvalidatedOrders.stream().map(order -> {
				return new ResUnvalidatedSalesOrderDTO(
						order.getIdSalesOrder(),
						order.getNoFaktur(),
						order.getCustomer().getNamaCustomer(),
						order.getTransactionType().name(),
						order.getTotalHarga()
				);
			}).collect(Collectors.toList());

			return ResponseUtil.success("Data Sales Order unvalidated ditemukan", dtoList);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal mengambil data unvalidated");
		}
	}

	private String generateFakturNumber() {
		return "SO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	}
}