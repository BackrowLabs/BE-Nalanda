package com.schoolmanagement.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.schoolmanagement.entity.FeePayment;
import com.schoolmanagement.entity.SchoolSettings;
import com.schoolmanagement.repository.SchoolSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PdfService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final SchoolSettingsRepository schoolSettingsRepository;

    public byte[] generateReceipt(FeePayment payment) {
        SchoolSettings settings = schoolSettingsRepository.findAll().stream()
                .findFirst()
                .orElse(null);

        String schoolName = settings != null ? settings.getSchoolName() : "School Management";
        String schoolAddress = settings != null && settings.getAddress() != null ? settings.getAddress() : "";
        String schoolPhone = settings != null && settings.getPhone() != null ? settings.getPhone() : "";

        var sf = payment.getStudentFee();
        var fi = sf.getFeeInstallment();
        var fs = fi.getFeeStructure();
        var student = sf.getStudent();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A5);
        document.setMargins(30, 40, 30, 40);

        // School header
        document.add(new Paragraph(schoolName)
                .setFontSize(16).setBold().setTextAlignment(TextAlignment.CENTER));
        if (!schoolAddress.isEmpty()) {
            document.add(new Paragraph(schoolAddress)
                    .setFontSize(9).setTextAlignment(TextAlignment.CENTER));
        }
        if (!schoolPhone.isEmpty()) {
            document.add(new Paragraph("Phone: " + schoolPhone)
                    .setFontSize(9).setTextAlignment(TextAlignment.CENTER));
        }

        document.add(new Paragraph(" "));

        // Receipt title
        document.add(new Paragraph("FEE RECEIPT")
                .setFontSize(13).setBold().setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(ColorConstants.LIGHT_GRAY).setPadding(4));

        document.add(new Paragraph(" "));

        // Receipt details table
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100));
        headerTable.addCell(plainCell("Receipt No: " + payment.getReceiptNumber()));
        headerTable.addCell(plainCell("Date: " + payment.getPaymentDate().format(DATE_FMT))
                .setTextAlignment(TextAlignment.RIGHT));
        document.add(headerTable);

        document.add(new Paragraph(" "));

        // Student details
        Table studentTable = new Table(UnitValue.createPercentArray(new float[]{35, 65}))
                .setWidth(UnitValue.createPercentValue(100));

        addRow(studentTable, "Student Name", student.getFullName());
        addRow(studentTable, "Admission No.", student.getAdmissionNumber());
        addRow(studentTable, "Class / Section",
                student.getSection() != null ? student.getSection().getFullName() : "—");
        addRow(studentTable, "Academic Year",
                fs.getAcademicYear() != null ? fs.getAcademicYear().getName() : "—");
        document.add(studentTable);

        document.add(new Paragraph(" "));

        // Fee details
        Table feeTable = new Table(UnitValue.createPercentArray(new float[]{35, 65}))
                .setWidth(UnitValue.createPercentValue(100));

        addRow(feeTable, "Fee Type", fs.getFeeType().name());
        addRow(feeTable, "Installment", "Installment " + fi.getInstallmentNumber());
        addRow(feeTable, "Due Date", fi.getDueDate().format(DATE_FMT));
        addRow(feeTable, "Amount Due", formatAmount(sf.getAmountDue()));
        if (sf.getLateFee().compareTo(BigDecimal.ZERO) > 0) {
            addRow(feeTable, "Late Fee", formatAmount(sf.getLateFee()));
        }
        addRow(feeTable, "Amount Paid", formatAmount(payment.getAmount()));
        addRow(feeTable, "Balance",
                formatAmount(sf.getAmountDue().add(sf.getLateFee()).subtract(sf.getAmountPaid())));
        document.add(feeTable);

        document.add(new Paragraph(" "));

        // Collected by
        Table footerTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100));
        footerTable.addCell(plainCell("Collected by: " + payment.getCollectedBy().getFullName()));
        if (payment.getApprovedBy() != null) {
            footerTable.addCell(plainCell("Approved by: " + payment.getApprovedBy().getFullName())
                    .setTextAlignment(TextAlignment.RIGHT));
        } else {
            footerTable.addCell(plainCell(""));
        }
        document.add(footerTable);

        document.add(new Paragraph(" "));
        document.add(new Paragraph("This is a computer-generated receipt.")
                .setFontSize(8).setItalic().setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY));

        document.close();
        return baos.toByteArray();
    }

    private Cell plainCell(String text) {
        return new Cell().add(new Paragraph(text).setFontSize(9))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
    }

    private void addRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setFontSize(9).setBold())
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setBackgroundColor(ColorConstants.LIGHT_GRAY).setPadding(3));
        table.addCell(new Cell().add(new Paragraph(value).setFontSize(9))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER).setPadding(3));
    }

    private String formatAmount(BigDecimal amount) {
        return "₹ " + String.format("%,.2f", amount);
    }
}
