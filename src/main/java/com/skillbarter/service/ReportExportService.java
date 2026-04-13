package com.skillbarter.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.skillbarter.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
// Handles report generation and export logic
@Service
@RequiredArgsConstructor
public class ReportExportService {

    private final UserRepository userRepo;
    private final SessionRepository sessionRepo;
    private final TransactionRepository transactionRepo;
    private final DisputeRepository disputeRepo;

    public byte[] exportUserReportPdf() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("SkillBarter User Report")
                    .setFontSize(20).setBold());
            document.add(new Paragraph("Generated: " + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
            document.add(new Paragraph("\n"));

            var users = userRepo.findAll();
            Table table = new Table(4);
            table.addHeaderCell("Username");
            table.addHeaderCell("Email");
            table.addHeaderCell("Credits");
            table.addHeaderCell("Status");

            users.forEach(u -> {
                table.addCell(u.getUsername());
                table.addCell(u.getEmail());
                table.addCell(u.getCreditBalance().toString());
                table.addCell(u.getStatus().toString());
            });

            document.add(table);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    public String exportUserReportCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("Username,Email,Credits,Status,Joined\n");
        
        userRepo.findAll().forEach(u -> {
            csv.append(u.getUsername()).append(",")
               .append(u.getEmail()).append(",")
               .append(u.getCreditBalance()).append(",")
               .append(u.getStatus()).append(",")
               .append(u.getCreatedAt()).append("\n");
        });
        
        return csv.toString();
    }

    public byte[] exportSessionReportPdf() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("SkillBarter Session Report")
                    .setFontSize(20).setBold());
            document.add(new Paragraph("Generated: " + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
            document.add(new Paragraph("\n"));

            var sessions = sessionRepo.findAll();
            Table table = new Table(5);
            table.addHeaderCell("ID");
            table.addHeaderCell("Skill");
            table.addHeaderCell("Teacher");
            table.addHeaderCell("Learner");
            table.addHeaderCell("Status");

            sessions.forEach(s -> {
                table.addCell(s.getId().toString());
                table.addCell(s.getSkill().getName());
                table.addCell(s.getTeacher().getUsername());
                table.addCell(s.getLearner().getUsername());
                table.addCell(s.getStatus().toString());
            });

            document.add(table);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    public String exportSessionReportCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Skill,Teacher,Learner,Status,Created\n");
        
        sessionRepo.findAll().forEach(s -> {
            csv.append(s.getId()).append(",")
               .append(s.getSkill().getName()).append(",")
               .append(s.getTeacher().getUsername()).append(",")
               .append(s.getLearner().getUsername()).append(",")
               .append(s.getStatus()).append(",")
               .append(s.getCreatedAt()).append("\n");
        });
        
        return csv.toString();
    }
}
