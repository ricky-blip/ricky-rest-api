package com.ricky.ricky_rest_api.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.ricky.ricky_rest_api.dto.response.DetailBarangDTO;
import com.ricky.ricky_rest_api.dto.response.ResDetailSalesOrderDTO;
import com.ricky.ricky_rest_api.dto.response.UserDTO;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Service
public class PdfService {

	public byte[] generateSalesOrderPdf(ResDetailSalesOrderDTO dto) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PdfWriter writer = new PdfWriter(outputStream);
		PdfDocument pdfDoc = new PdfDocument(writer);
		Document document = new Document(pdfDoc, PageSize.A4);

		// Set margins
		document.setMargins(40, 40, 40, 40);

		// Load fonts
		PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");
		PdfFont regularFont = PdfFontFactory.createFont("Helvetica");

		// Colors
		DeviceRgb headerColor = new DeviceRgb(41, 128, 185);
		DeviceRgb tableHeaderColor = new DeviceRgb(236, 240, 241);

		// Header
		document.add(new Paragraph("Selo - Sales Order")
				.setFont(boldFont)
				.setFontSize(18)
				.setFontColor(headerColor)
				.setTextAlignment(TextAlignment.CENTER));

		document.add(new Paragraph("No. Faktur: " + dto.getNoFaktur())
				.setFont(boldFont)
				.setFontSize(12));

		document.add(new Paragraph("Tanggal: " + dto.getTanggalOrder())
				.setFont(regularFont)
				.setFontSize(11));

		document.add(new Paragraph("\n"));

		// Customer Section
		Table customerTable = new Table(2);
		customerTable.setWidth(UnitValue.createPercentValue(100));

		// Customer Info
		Cell customerCell = new Cell();
		customerCell.setBorder(Border.NO_BORDER);
		customerCell.add(new Paragraph("CUSTOMER INFORMATION")
				.setFont(boldFont)
				.setFontSize(11)
				.setMarginBottom(5));
		customerCell.add(new Paragraph("Customer: " + dto.getNamaCustomer())
				.setFont(boldFont)
				.setFontSize(10));
		customerCell.add(new Paragraph("Alamat: " + dto.getAlamatCustomer())
				.setFont(regularFont)
				.setFontSize(10));
		customerCell.add(new Paragraph("Telepon: " + dto.getPhoneCustomer())
				.setFont(regularFont)
				.setFontSize(10));
		customerCell.add(new Paragraph("Email: " + dto.getEmailCustomer())
				.setFont(regularFont)
				.setFontSize(10));

		// Sales Info
		Cell salesCell = new Cell();
		salesCell.setBorder(Border.NO_BORDER);
		UserDTO salesPerson = dto.getSalesPerson();
		salesCell.add(new Paragraph("SALES INFORMATION")
				.setFont(boldFont)
				.setFontSize(11)
				.setMarginBottom(5));
		salesCell.add(new Paragraph("Sales: " + salesPerson.getFullName())
				.setFont(boldFont)
				.setFontSize(10));
		salesCell.add(new Paragraph("Username: " + salesPerson.getUsername())
				.setFont(regularFont)
				.setFontSize(10));
		salesCell.add(new Paragraph("Status: " + dto.getStatus())
				.setFont(regularFont)
				.setFontSize(10));

		customerTable.addCell(customerCell);
		customerTable.addCell(salesCell);
		document.add(customerTable);
		document.add(new Paragraph("\n"));

		// Items Table
		Table table = new Table(4);
		table.setWidth(UnitValue.createPercentValue(100));

		// Header cells
		addHeaderCell(table, "Barang", boldFont, tableHeaderColor);
		addHeaderCell(table, "Qty", boldFont, tableHeaderColor);
		addHeaderCell(table, "Harga", boldFont, tableHeaderColor);
		addHeaderCell(table, "Total", boldFont, tableHeaderColor);

		// Data rows
		for (DetailBarangDTO detail : dto.getDetails()) {
			table.addCell(new Cell().add(new Paragraph(detail.getNamaBarang())
					.setFont(regularFont).setFontSize(10)));
			table.addCell(new Cell().add(new Paragraph(String.valueOf(detail.getQuantity()))
					.setFont(regularFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER)));
			table.addCell(new Cell().add(new Paragraph(formatCurrency(detail.getHargaJual()))
					.setFont(regularFont).setFontSize(10).setTextAlignment(TextAlignment.RIGHT)));

			BigDecimal total = detail.getHargaJual().multiply(BigDecimal.valueOf(detail.getQuantity()));
			table.addCell(new Cell().add(new Paragraph(formatCurrency(total))
					.setFont(regularFont).setFontSize(10).setTextAlignment(TextAlignment.RIGHT)));
		}
		document.add(table);

		// Total Section
		document.add(new Paragraph("\n"));

		Table summaryTable = new Table(2);
		summaryTable.setWidth(UnitValue.createPercentValue(40));
		summaryTable.setHorizontalAlignment(com.itextpdf.layout.property.HorizontalAlignment.RIGHT);

		addSummaryRow(summaryTable, "Subtotal:", formatCurrency(dto.getSubtotal()), regularFont);
		addSummaryRow(summaryTable, "PPN:", formatCurrency(dto.getJumlahPpn()), regularFont);

		// Total with bold styling
		Cell totalLabelCell = new Cell().setBorder(Border.NO_BORDER)
				.add(new Paragraph("TOTAL:").setFont(boldFont).setFontSize(12)
						.setTextAlignment(TextAlignment.RIGHT));
		Cell totalValueCell = new Cell().setBorder(Border.NO_BORDER)
				.add(new Paragraph(formatCurrency(dto.getTotalHarga())).setFont(boldFont).setFontSize(12)
						.setTextAlignment(TextAlignment.RIGHT));

		summaryTable.addCell(totalLabelCell);
		summaryTable.addCell(totalValueCell);
		document.add(summaryTable);

		document.close();
		return outputStream.toByteArray();
	}

	private void addHeaderCell(Table table, String text, PdfFont font, DeviceRgb backgroundColor) {
		Cell cell = new Cell();
		cell.setBackgroundColor(backgroundColor);
		cell.setBorder(new SolidBorder(ColorConstants.GRAY, 1));
		cell.setPadding(8);
		cell.add(new Paragraph(text).setFont(font).setFontSize(10).setTextAlignment(TextAlignment.CENTER));
		table.addHeaderCell(cell);
	}

	private void addSummaryRow(Table table, String label, String value, PdfFont font) {
		Cell labelCell = new Cell().setBorder(Border.NO_BORDER)
				.add(new Paragraph(label).setFont(font).setFontSize(11).setTextAlignment(TextAlignment.RIGHT));
		Cell valueCell = new Cell().setBorder(Border.NO_BORDER)
				.add(new Paragraph(value).setFont(font).setFontSize(11).setTextAlignment(TextAlignment.RIGHT));

		table.addCell(labelCell);
		table.addCell(valueCell);
	}

	private String formatCurrency(BigDecimal amount) {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("id", "ID"));
		symbols.setGroupingSeparator('.');
		symbols.setDecimalSeparator(',');
		DecimalFormat formatter = new DecimalFormat("#,##0", symbols);
		return "Rp " + formatter.format(amount);
	}
}