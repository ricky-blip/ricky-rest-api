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

  //Save Ke Draft
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

			// 4. Validasi: Semua barang harus punya satuan yang sama
			String satuanPertama = null;
			for (ValSalesOrderDetailDTO detailDto : dto.getDetails()) {
				Barang barang = barangRepository.findById(detailDto.getIdBarang())
						.orElseThrow(() -> new RuntimeException("Barang tidak ditemukan"));

				if (satuanPertama == null) {
					satuanPertama = barang.getSatuan();
				} else if (!satuanPertama.equals(barang.getSatuan())) {
					return ResponseUtil.badRequest("Tidak boleh input barang dengan satuan berbeda. Satuan harus: " + satuanPertama);
				}

				// Validasi stok: quantity tidak boleh melebihi available_qty
				if (detailDto.getQuantity() > barang.getAvailableQty()) {
					return ResponseUtil.badRequest("Stok tidak mencukupi untuk barang: " + barang.getNamaBarang() +
							". Available: " + barang.getAvailableQty() + ", Diminta: " + detailDto.getQuantity());
				}
			}

			// 5. Proses Detail & Hitung Subtotal
			BigDecimal subtotal = BigDecimal.ZERO;
			for (ValSalesOrderDetailDTO detailDto : dto.getDetails()) {
				Barang barang = barangRepository.findById(detailDto.getIdBarang())
						.orElseThrow(() -> new RuntimeException("Barang tidak ditemukan"));

				SalesOrderDetail detail = new SalesOrderDetail();
				detail.setBarang(barang);
				detail.setAddress(detailDto.getAddress());
				detail.setQuantity(detailDto.getQuantity());
				detail.setHargaJual(detailDto.getHargaJual());
				detail.setAddress(detailDto.getAddress());
				detail.setSalesOrder(salesOrder);

				subtotal = subtotal.add(detail.getHargaJual().multiply(BigDecimal.valueOf(detail.getQuantity())));
				salesOrder.addDetail(detail);
			}

			// 6. Hitung PPN & Total
			BigDecimal ppnRate = new BigDecimal("10.00"); // 10%
			BigDecimal jumlahPpn = subtotal.multiply(ppnRate.divide(new BigDecimal("100")));
			BigDecimal totalHarga = subtotal.add(jumlahPpn);

			salesOrder.setSubtotal(subtotal);
			salesOrder.setPpn(ppnRate);
			salesOrder.setJumlahPpn(jumlahPpn);
			salesOrder.setTotalHarga(totalHarga);

			// 7. Simpan
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
				detail.setAddress(detailDto.getAddress());
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
						barang.getIdBarang(),
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

	@Transactional
	public ResponseEntity<Object> reject(Long id, HttpServletRequest request) {
		try {
			SalesOrder salesOrder = salesOrderRepository.findById(id)
					.orElseThrow(() -> new RuntimeException("Sales Order tidak ditemukan"));

			// Hanya bisa reject jika status UNVALIDATED
			if (salesOrder.getStatus() != OrderStatus.UNVALIDATED) {
				return ResponseUtil.badRequest("Hanya Sales Order dengan status UNVALIDATED yang bisa direject");
			}

			// Ubah status jadi REJECTED
			salesOrder.setStatus(OrderStatus.REJECTED);
			salesOrderRepository.save(salesOrder);

			return ResponseUtil.success("Sales Order berhasil ditolak", null);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal menolak Sales Order");
		}
	}

	// File: service/SalesOrderService.java

	@Transactional
	public ResponseEntity<Object> approve(Long id, HttpServletRequest request) {
		try {
			SalesOrder salesOrder = salesOrderRepository.findById(id)
					.orElseThrow(() -> new RuntimeException("Sales Order tidak ditemukan"));

			// Hanya bisa approve jika status UNVALIDATED
			if (salesOrder.getStatus() != OrderStatus.UNVALIDATED) {
				return ResponseUtil.badRequest("Hanya Sales Order dengan status UNVALIDATED yang bisa di-approve");
			}

			// Simpan referensi user yang approve
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User approvingUser = userRepository.findByUsername(authentication.getName())
					.orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

			// Cek dan update stok untuk setiap barang
			for (SalesOrderDetail detail : salesOrder.getDetails()) {
				Barang barang = detail.getBarang();

				// Cek ketersediaan stok
				if (barang.getAvailableQty() < detail.getQuantity()) {
					return ResponseUtil.badRequest("Stok tidak cukup untuk barang: " + barang.getNamaBarang());
				}

				// Update stok
				barang.setReservedQty(barang.getReservedQty() + detail.getQuantity());
				barang.setAvailableQty(barang.getAvailableQty() - detail.getQuantity());

				barangRepository.save(barang);
			}

			// Ubah status jadi VALIDATED
			salesOrder.setStatus(OrderStatus.VALIDATED);
			salesOrder.setSalesManager(approvingUser); // Siapa yang approve
			salesOrder.setTanggalValidasi(LocalDateTime.now());
			salesOrderRepository.save(salesOrder);

			return ResponseUtil.success("Sales Order berhasil disetujui", null);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal menyetujui Sales Order");
		}
	}

	public ResponseEntity<Object> findAllValidated(HttpServletRequest request) {
		try {
			List<SalesOrder> validatedOrders = salesOrderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.VALIDATED);

			List<ResDraftSalesOrderDTO> dtoList = validatedOrders.stream().map(order -> {
				return new ResDraftSalesOrderDTO(
						order.getIdSalesOrder(),
						order.getNoFaktur(),
						order.getCustomer().getNamaCustomer(),
						order.getTransactionType().name(),
						order.getTotalHarga()
				);
			}).collect(Collectors.toList());

			return ResponseUtil.success("Data Sales Order yang sudah divalidasi ditemukan", dtoList);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal mengambil data yang sudah divalidasi");
		}
	}

	public ResponseEntity<Object> findAllRejected(HttpServletRequest request) {
		try {
			List<SalesOrder> rejectedOrders = salesOrderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.REJECTED);

			List<ResDraftSalesOrderDTO> dtoList = rejectedOrders.stream().map(order -> {
				return new ResDraftSalesOrderDTO(
						order.getIdSalesOrder(),
						order.getNoFaktur(),
						order.getCustomer().getNamaCustomer(),
						order.getTransactionType().name(),
						order.getTotalHarga()
				);
			}).collect(Collectors.toList());

			return ResponseUtil.success("Data Sales Order yang ditolak ditemukan", dtoList);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal mengambil data yang ditolak");
		}
	}

	public ResponseEntity<Object> getSummary(HttpServletRequest request) {
		try {
			ResSalesOrderSummaryDTO summary = new ResSalesOrderSummaryDTO();

			// Hitung per status
			summary.setPending(
					salesOrderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.PENDING).size()
			);
			summary.setUnvalidated(
					salesOrderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.UNVALIDATED).size()
			);
			summary.setValidated(
					salesOrderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.VALIDATED).size()
			);
			summary.setRejected(
					salesOrderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.REJECTED).size()
			);

			return ResponseUtil.success("Ringkasan Sales Order berhasil didapatkan", summary);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal mengambil ringkasan Sales Order");
		}
	}

	private String generateFakturNumber() {
		return "SO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	}
}