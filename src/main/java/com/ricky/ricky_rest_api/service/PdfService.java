package com.ricky.ricky_rest_api.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.property.TextAlignment;
import com.ricky.ricky_rest_api.dto.response.DetailBarangDTO;
import com.ricky.ricky_rest_api.dto.response.ResDetailSalesOrderDTO;
import com.ricky.ricky_rest_api.dto.response.UserDTO;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

@Service
public class PdfService {

	public byte[] generateSalesOrderPdf(ResDetailSalesOrderDTO dto) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PdfWriter writer = new PdfWriter(outputStream);
		PdfDocument pdfDoc = new PdfDocument(writer);
		Document document = new Document(pdfDoc);

		// Header
		document.add(new Paragraph("Selo - Sales Order")
				.setFontSize(20)
				.setBold()
				.setTextAlignment(TextAlignment.CENTER));
		document.add(new Paragraph("No. Faktur: " + dto.getNoFaktur()));
		document.add(new Paragraph("Tanggal: " + dto.getTanggalOrder()));
		document.add(new Paragraph("\n"));

		// Customer
		document.add(new Paragraph("Customer: " + dto.getNamaCustomer()).setBold());
		document.add(new Paragraph("Alamat: " + dto.getAlamatCustomer()));
		document.add(new Paragraph("Telepon: " + dto.getPhoneCustomer()));
		document.add(new Paragraph("Email: " + dto.getEmailCustomer()));
		document.add(new Paragraph("\n"));

		// Sales Person
		UserDTO salesPerson = dto.getSalesPerson();
		document.add(new Paragraph("Sales: " + salesPerson.getFullName() + " (" + salesPerson.getUsername() + ")"));
		document.add(new Paragraph("Status: " + dto.getStatus()));
		document.add(new Paragraph("\n"));

		// Tabel Barang
		Table table = new Table(4);
		table.addHeaderCell("Barang");
		table.addHeaderCell("Qty");
		table.addHeaderCell("Harga");
		table.addHeaderCell("Total");

		for (DetailBarangDTO detail : dto.getDetails()) {
			table.addCell(detail.getNamaBarang());
			table.addCell(String.valueOf(detail.getQuantity()));
			table.addCell("Rp " + detail.getHargaJual().toString());
			BigDecimal total = detail.getHargaJual().multiply(BigDecimal.valueOf(detail.getQuantity()));
			table.addCell("Rp " + total.toString());
		}
		document.add(table);

		// Total
		document.add(new Paragraph("\n"));
		document.add(new Paragraph("Subtotal: Rp " + dto.getSubtotal().toString()));
		document.add(new Paragraph("PPN: Rp " + dto.getJumlahPpn().toString()));
		document.add(new Paragraph("Total: Rp " + dto.getTotalHarga().toString()).setBold());

		document.close();
		return outputStream.toByteArray();
	}
}