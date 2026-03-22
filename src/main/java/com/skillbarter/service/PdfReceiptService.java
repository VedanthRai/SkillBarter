package com.skillbarter.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.skillbarter.entity.Session;
import com.skillbarter.entity.Transaction;
import com.skillbarter.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * PDF Receipt Service — Minor Feature 4: Usage Report Generation.
 *
 * Generates a professional session receipt PDF using iText 7.
 * Called automatically upon session completion.
 *
 * SOLID – SRP: handles only PDF generation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PdfReceiptService {

    private final TransactionRepository transactionRepository;

    @Value("${app.pdf.output.dir:./receipts}")
    private String pdfOutputDir;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(79, 70, 229);   // indigo
    private static final DeviceRgb ACCENT_COLOR  = new DeviceRgb(16, 185, 129);  // emerald

    public String generateReceipt(Session session) throws IOException {
        Files.createDirectories(Paths.get(pdfOutputDir));
        String filename = "receipt_session_" + session.getId() + ".pdf";
        String filepath = Paths.get(pdfOutputDir, filename).toString();

        Optional<Transaction> txOpt = transactionRepository.findBySessionId(session.getId());

        try (PdfWriter writer = new PdfWriter(filepath);
             PdfDocument pdf = new PdfDocument(writer);
             Document doc = new Document(pdf)) {

            doc.setMargins(36, 54, 36, 54);

            // ── Header ────────────────────────────────────────────────
            Paragraph header = new Paragraph("SkillBarter")
                    .setFontSize(28)
                    .setBold()
                    .setFontColor(PRIMARY_COLOR)
                    .setTextAlignment(TextAlignment.CENTER);
            doc.add(header);

            Paragraph subtitle = new Paragraph("Session Receipt")
                    .setFontSize(14)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            doc.add(subtitle);

            // ── Divider ───────────────────────────────────────────────
            doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine())
                    .setMarginBottom(16));

            // ── Session Info Table ────────────────────────────────────
            Table table = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                    .useAllAvailableWidth()
                    .setMarginBottom(20);

            addRow(table, "Receipt ID",    "SB-" + session.getId());
            addRow(table, "Date",          session.getCompletedAt() != null
                    ? session.getCompletedAt().format(DATE_FMT) : "N/A");
            addRow(table, "Skill",         session.getSkill().getName());
            addRow(table, "Category",      session.getSkill().getCategory().name());
            addRow(table, "Duration",      session.getDurationMinutes() + " minutes");
            addRow(table, "Scheduled At",  session.getScheduledAt().format(DATE_FMT));

            doc.add(table);

            // ── Parties ───────────────────────────────────────────────
            Paragraph partiesHeader = new Paragraph("Parties")
                    .setFontSize(13).setBold().setFontColor(PRIMARY_COLOR).setMarginBottom(8);
            doc.add(partiesHeader);

            Table parties = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .useAllAvailableWidth().setMarginBottom(20);
            Cell learnerCell = new Cell().add(new Paragraph("🎓 Learner")
                    .setBold().setFontColor(PRIMARY_COLOR))
                    .add(new Paragraph(session.getLearner().getUsername()))
                    .add(new Paragraph(session.getLearner().getEmail()).setFontColor(ColorConstants.GRAY));
            Cell teacherCell = new Cell().add(new Paragraph("🏫 Teacher")
                    .setBold().setFontColor(ACCENT_COLOR))
                    .add(new Paragraph(session.getTeacher().getUsername()))
                    .add(new Paragraph(session.getTeacher().getEmail()).setFontColor(ColorConstants.GRAY));
            parties.addCell(learnerCell);
            parties.addCell(teacherCell);
            doc.add(parties);

            // ── Transaction Summary ───────────────────────────────────
            Paragraph txHeader = new Paragraph("Transaction Summary")
                    .setFontSize(13).setBold().setFontColor(PRIMARY_COLOR).setMarginBottom(8);
            doc.add(txHeader);

            Table txTable = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                    .useAllAvailableWidth().setMarginBottom(20);

            txOpt.ifPresent(tx -> {
                addRow(txTable, "Transaction ID", "TX-" + tx.getId());
                addRow(txTable, "Status", tx.getStatus().name());
                addRow(txTable, "Amount", tx.getAmount() + " credits");
                if (tx.getEscrowedAt() != null)
                    addRow(txTable, "Escrowed At", tx.getEscrowedAt().format(DATE_FMT));
                if (tx.getResolvedAt() != null)
                    addRow(txTable, "Released At", tx.getResolvedAt().format(DATE_FMT));
            });
            doc.add(txTable);

            // ── Footer ────────────────────────────────────────────────
            doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine())
                    .setMarginTop(10).setMarginBottom(10));
            doc.add(new Paragraph(
                    "This is an auto-generated receipt from SkillBarter. " +
                    "Time credits are not legal tender. " +
                    "For disputes contact support@skillbarter.app")
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));
        }

        log.info("PDF receipt generated: {}", filepath);
        return filename;
    }

    private void addRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold())
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setPadding(4));
        table.addCell(new Cell().add(new Paragraph(value))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setPadding(4));
    }
}
