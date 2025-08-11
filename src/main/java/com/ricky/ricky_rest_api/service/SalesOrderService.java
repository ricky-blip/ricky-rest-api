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

	@Autowired
	private FcmService fcmService;

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

		  // 4. VALIDASI: Cek stok tersedia & satuan sama
		  String satuanPertama = null;
		  BigDecimal subtotal = BigDecimal.ZERO;

		  for (ValSalesOrderDetailDTO detailDto : dto.getDetails()) {
			  Barang barang = barangRepository.findById(detailDto.getIdBarang())
					  .orElseThrow(() -> new RuntimeException("Barang tidak ditemukan"));

			  // Cek satuan harus sama
			  if (satuanPertama == null) {
				  satuanPertama = barang.getSatuan();
			  } else if (!satuanPertama.equals(barang.getSatuan())) {
				  return ResponseUtil.badRequest("Tidak boleh input barang dengan satuan berbeda. Satuan harus: " + satuanPertama);
			  }

			  // Cek stok tersedia
			  if (detailDto.getQuantity() > barang.getAvailableQty()) {
				  return ResponseUtil.badRequest("Stok tidak mencukupi untuk barang: " + barang.getNamaBarang() +
						  ". Available: " + barang.getAvailableQty() + ", Diminta: " + detailDto.getQuantity());
			  }

			  // Buat detail
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
		  BigDecimal ppnRate = new BigDecimal("10.00");
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

	//Get All Draft
	@Override
	public ResponseEntity<Object> findAll(HttpServletRequest request) {
		try {
			// 1. Ambil user dari JWT
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication == null || !authentication.isAuthenticated()) {
				return ResponseUtil.unauthorized("User tidak terautentikasi");
			}
			String username = authentication.getName();

			User currentUser = userRepository.findByUsername(username)
					.orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

			List<SalesOrder> drafts;

			// 2. Filter: Sales hanya lihat milik sendiri, Sales Manager lihat semua
			if (currentUser.getRole().equals(Role.SALES_MANAGER)) {
				drafts = salesOrderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.PENDING);
			} else {
				drafts = salesOrderRepository.findByStatusAndSalesPersonOrderByCreatedAtDesc(
						OrderStatus.PENDING, currentUser);
			}

			// 3. Cek apakah kosong
			if (drafts.isEmpty()) {
				return ResponseUtil.notFound("Data draft Sales Order tidak ditemukan");
			}

			// 4. Mapping ke DTO
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

	//GET Detail
	@Override
	public ResponseEntity<Object> findById(Long id, HttpServletRequest request) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User currentUser = userRepository.findByUsername(authentication.getName())
					.orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

			SalesOrder salesOrder = salesOrderRepository.findById(id)
					.orElseThrow(() -> new RuntimeException("Sales Order tidak ditemukan"));


			// Cek otorisasi: pemilik atau Sales Manager
			if (!salesOrder.getSalesPerson().getIdUser().equals(currentUser.getIdUser())
					&& !currentUser.getRole().equals(Role.SALES_MANAGER)) {
				return ResponseUtil.forbidden("Anda tidak memiliki izin untuk mengakses data ini");
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

			return ResponseUtil.success("Data Sales Order ditemukan", dto);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal mengambil detail Sales Order");
		}
	}

	//EDIT Data Draft
	@Transactional
	public ResponseEntity<Object> editSalesOrder(Long id, ValSalesOrderEditDTO dto, HttpServletRequest request) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User currentUser = userRepository.findByUsername(authentication.getName())
					.orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

			SalesOrder salesOrder = salesOrderRepository.findById(id)
					.orElseThrow(() -> new RuntimeException("Sales Order tidak ditemukan"));

			// Cek pemilik
			if (!isOwner(id, currentUser) && !currentUser.getRole().equals(Role.SALES_MANAGER)) {
				return ResponseUtil.forbidden("Anda tidak memiliki izin untuk mengedit draft ini");
			}

			if (!salesOrder.getStatus().equals(OrderStatus.PENDING)) {
				return ResponseUtil.badRequest("Hanya Sales Order dengan status PENDING yang bisa diedit");
			}

			// 1. VALIDASI STOK DULU (sebelum clear)
			for (ValSalesOrderDetailEditDTO detailDto : dto.getDetails()) {
				Barang barang = barangRepository.findById(detailDto.getIdBarang())
						.orElseThrow(() -> new RuntimeException("Barang tidak ditemukan"));

				if (detailDto.getQuantity() > barang.getAvailableQty()) {
					return ResponseUtil.badRequest(
							"Stok tidak mencukupi untuk barang: " + barang.getNamaBarang() +
									". Available: " + barang.getAvailableQty() + ", Diminta: " + detailDto.getQuantity()
					);
				}
			}

			// 2. Baru clear setelah validasi berhasil
			salesOrder.getDetails().clear();

			// 3. Proses input baru
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

			// 4. Hitung ulang
			hitungUlangTotal(salesOrder);

			// 5. Simpan
			salesOrderRepository.save(salesOrder);

			return ResponseUtil.success("Sales Order berhasil diperbarui", null);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal memperbarui Sales Order");
		}
	}

	private void hitungUlangTotal(SalesOrder salesOrder) {
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
	}

	//Delete Data Draft (Hard Code delete langsung ke database)
	@Override
	@Transactional
	public ResponseEntity<Object> delete(Long id, HttpServletRequest request) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User currentUser = userRepository.findByUsername(authentication.getName())
					.orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

			SalesOrder salesOrder = salesOrderRepository.findById(id)
					.orElseThrow(() -> new RuntimeException("Sales Order tidak ditemukan"));

			// Cek pemilik
			if (!isOwner(id, currentUser)) {
				return ResponseUtil.forbidden("Anda hanya bisa menghapus draft milik sendiri");
			}

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

	//save draft ke approval (SUBMIT)
	@Transactional
	public ResponseEntity<Object> submitToApproval(Long id, HttpServletRequest request) {
		try {
			SalesOrder salesOrder = salesOrderRepository.findById(id)
					.orElseThrow(() -> new RuntimeException("Sales Order tidak ditemukan"));

			// Hanya draft (PENDING) yang bisa dikirim ke approval
			if (salesOrder.getStatus() != OrderStatus.PENDING) {
				return ResponseUtil.badRequest("Hanya Sales Order dengan status PENDING yang bisa dikirim ke approval");
			}

			// Cek stok saat submit
			for (SalesOrderDetail detail : salesOrder.getDetails()) {
				Barang barang = detail.getBarang();
				if (detail.getQuantity() > barang.getAvailableQty()) {
					return ResponseUtil.badRequest(
							"Stok tidak mencukupi untuk barang: " + barang.getNamaBarang() +
									". Available: " + barang.getAvailableQty() + ", Diminta: " + detail.getQuantity()
					);
				}
			}

			// Ubah status ke UNVALIDATED
			salesOrder.setStatus(OrderStatus.UNVALIDATED);
			salesOrderRepository.save(salesOrder);

			// Kirim notifikasi ke Sales Manager
			List<User> salesManagers = userRepository.findByRole(Role.SALES_MANAGER);
			for (User manager : salesManagers) {
				if (manager.getFcmToken() != null) {
					fcmService.sendNotification(
							manager.getFcmToken(),
							"Draft Baru Menunggu Approval",
							"Sales " + salesOrder.getSalesPerson().getFullName() +
									" telah mengirim draft " + salesOrder.getNoFaktur() +
									" untuk approval."
					);
				}
			}

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
	}

	@Override
	public ResponseEntity<Object> findByParam(String column, String value, HttpServletRequest request) {
		return ResponseUtil.notFound("Fitur pencarian belum diimplementasikan");
	}

	//------------ SECTION : APPROVAL SO
	//Get Data Unvalidated
	public ResponseEntity<Object> findAllUnvalidated(HttpServletRequest request) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User currentUser = userRepository.findByUsername(authentication.getName())
					.orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

			List<SalesOrder> unvalidatedOrders;

			if (currentUser.getRole().equals(Role.SALES_MANAGER)) {
				// Sales Manager: lihat semua
				unvalidatedOrders = salesOrderRepository
						.findByStatusOrderByCreatedAtDesc(OrderStatus.UNVALIDATED);
			} else {
				// Sales: hanya lihat milik sendiri
				unvalidatedOrders = salesOrderRepository
						.findByStatusAndSalesPersonOrderByCreatedAtDesc(OrderStatus.UNVALIDATED, currentUser);
			}

			if (unvalidatedOrders.isEmpty()) {
				return ResponseUtil.notFound("Data Sales Order unvalidated tidak ditemukan");
			}

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

	//BUTTON Reject
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

			return ResponseUtil.success("Sales Order berhasil direject", null);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal menolak Sales Order");
		}
	}

	//BUTTON Approve
	@Transactional
	public ResponseEntity<Object> approve(Long id, HttpServletRequest request) {
		try {
			// findById() akan pakai PESSIMISTIC_WRITE
			SalesOrder salesOrder = salesOrderRepository.findById(id)
					.orElseThrow(() -> new RuntimeException("Sales Order tidak ditemukan"));

			if (salesOrder.getStatus() != OrderStatus.UNVALIDATED) {
				return ResponseUtil.badRequest("Hanya Sales Order dengan status UNVALIDATED yang bisa di-approve");
			}

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User approvingUser = userRepository.findByUsername(authentication.getName())
					.orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

			for (SalesOrderDetail detail : salesOrder.getDetails()) {
				// findById() akan pakai PESSIMISTIC_WRITE
				Barang barang = barangRepository.findById(detail.getBarang().getIdBarang())
						.orElseThrow(() -> new RuntimeException("Barang tidak ditemukan"));

				if (barang.getAvailableQty() < detail.getQuantity()) {
					return ResponseUtil.badRequest("Stok tidak cukup untuk barang: " + barang.getNamaBarang());
				}

				barang.setReservedQty(barang.getReservedQty() + detail.getQuantity());
				barang.setAvailableQty(barang.getAvailableQty() - detail.getQuantity());
				barangRepository.save(barang);
			}

			salesOrder.setStatus(OrderStatus.VALIDATED);
			salesOrder.setSalesManager(approvingUser);
			salesOrder.setTanggalValidasi(LocalDateTime.now());
			salesOrderRepository.save(salesOrder);

			return ResponseUtil.success("Sales Order berhasil disetujui", null);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal menyetujui Sales Order");
		}
	}

	//Get Data Validated
	public ResponseEntity<Object> findAllValidated(HttpServletRequest request) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User currentUser = userRepository.findByUsername(authentication.getName())
					.orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

			List<SalesOrder> validatedOrders;

			if (currentUser.getRole().equals(Role.SALES_MANAGER)) {
				validatedOrders = salesOrderRepository
						.findByStatusOrderByCreatedAtDesc(OrderStatus.VALIDATED);
			} else {
				validatedOrders = salesOrderRepository
						.findByStatusAndSalesPersonOrderByCreatedAtDesc(OrderStatus.VALIDATED, currentUser);
			}

			if (validatedOrders.isEmpty()) {
				return ResponseUtil.notFound("Data Sales Order yang sudah divalidasi tidak ditemukan");
			}

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

	//Get Data Rejected
	public ResponseEntity<Object> findAllRejected(HttpServletRequest request) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User currentUser = userRepository.findByUsername(authentication.getName())
					.orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

			List<SalesOrder> rejectedOrders;

			if (currentUser.getRole().equals(Role.SALES_MANAGER)) {
				rejectedOrders = salesOrderRepository
						.findByStatusOrderByCreatedAtDesc(OrderStatus.REJECTED);
			} else {
				rejectedOrders = salesOrderRepository
						.findByStatusAndSalesPersonOrderByCreatedAtDesc(OrderStatus.REJECTED, currentUser);
			}

			if (rejectedOrders.isEmpty()) {
				return ResponseUtil.notFound("Data Sales Order yang ditolak tidak ditemukan");
			}

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

	//Get Data Hitungan Per Status
	public ResponseEntity<Object> getSummary(HttpServletRequest request) {
		try {
			ResSalesOrderSummaryDTO summary = new ResSalesOrderSummaryDTO();

			// Hitung per status
			int pending = salesOrderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.PENDING).size();
			int unvalidated = salesOrderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.UNVALIDATED).size();
			int validated = salesOrderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.VALIDATED).size();
			int rejected = salesOrderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.REJECTED).size();

			summary.setPending(pending);
			summary.setUnvalidated(unvalidated);
			summary.setValidated(validated);
			summary.setRejected(rejected);

			// Cek jika semua status bernilai 0
			if (pending == 0 && unvalidated == 0 && validated == 0 && rejected == 0) {
				return ResponseUtil.notFound("Tidak ada data Sales Order yang tersedia");
			}

			return ResponseUtil.success("Ringkasan Sales Order berhasil didapatkan", summary);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal mengambil ringkasan Sales Order");
		}
	}

	//SECTION : Search Data
	public ResponseEntity<Object> searchDrafts(String query, HttpServletRequest request) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User currentUser = userRepository.findByUsername(authentication.getName())
					.orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

			if (query == null || query.trim().isEmpty()) {
				return ResponseUtil.badRequest("Query pencarian tidak boleh kosong");
			}

			List<SalesOrder> results = salesOrderRepository.searchByStatusAndQuery(OrderStatus.PENDING, query.trim());

			// Filter: Sales hanya lihat milik sendiri
			if (!currentUser.getRole().equals(Role.SALES_MANAGER)) {
				results = results.stream()
						.filter(so -> so.getSalesPerson().getIdUser().equals(currentUser.getIdUser()))
						.collect(Collectors.toList());
			}

			if (results.isEmpty()) {
				return ResponseUtil.notFound("Tidak ada draft yang cocok");
			}

			List<ResDraftSalesOrderDTO> dtos = results.stream().map(order -> {
				return new ResDraftSalesOrderDTO(
						order.getIdSalesOrder(),
						order.getNoFaktur(),
						order.getCustomer().getNamaCustomer(),
						order.getTransactionType().name(),
						order.getTotalHarga()
				);
			}).collect(Collectors.toList());

			return ResponseUtil.success("Draft ditemukan", dtos);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal mencari draft");
		}
	}

	public ResponseEntity<Object> searchUnvalidated(String query, HttpServletRequest request) {
		return searchByStatus(OrderStatus.UNVALIDATED, query, request);
	}

	public ResponseEntity<Object> searchRejected(String query, HttpServletRequest request) {
		return searchByStatus(OrderStatus.REJECTED, query, request);
	}

	public ResponseEntity<Object> searchValidated(String query, HttpServletRequest request) {
		return searchByStatus(OrderStatus.VALIDATED, query, request);
	}

	private ResponseEntity<Object> searchByStatus(OrderStatus status, String query, HttpServletRequest request) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User currentUser = userRepository.findByUsername(authentication.getName())
					.orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

			if (query == null || query.trim().isEmpty()) {
				return ResponseUtil.badRequest("Query pencarian tidak boleh kosong");
			}

			List<SalesOrder> results = salesOrderRepository.searchByStatusAndQuery(status, query.trim());

			// Filter: Sales hanya lihat milik sendiri
			if (!currentUser.getRole().equals(Role.SALES_MANAGER)) {
				results = results.stream()
						.filter(so -> so.getSalesPerson().getIdUser().equals(currentUser.getIdUser()))
						.collect(Collectors.toList());
			}

			if (results.isEmpty()) {
				return ResponseUtil.notFound("Tidak ada data yang cocok");
			}

			List<ResUnvalidatedSalesOrderDTO> dtos = results.stream().map(order -> {
				return new ResUnvalidatedSalesOrderDTO(
						order.getIdSalesOrder(),
						order.getNoFaktur(),
						order.getCustomer().getNamaCustomer(),
						order.getTransactionType().name(),
						order.getTotalHarga()
				);
			}).collect(Collectors.toList());

			return ResponseUtil.success("Data ditemukan", dtos);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.serverError("Gagal mencari data");
		}
	}

	//cek pemilik data (sales)
	private boolean isOwner(Long salesOrderId, User currentUser) {
		return salesOrderRepository.findById(salesOrderId)
				.map(so -> so.getSalesPerson().getIdUser().equals(currentUser.getIdUser()))
				.orElse(false);
	}

	private String generateFakturNumber() {
		return "SO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	}
}