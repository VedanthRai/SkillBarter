package com.skillbarter.controller;

import com.skillbarter.service.ReportExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ReportExportController {

    private final ReportExportService reportExportService;

    @GetMapping("/users/pdf")
    public ResponseEntity<byte[]> exportUsersPdf() {
        byte[] pdf = reportExportService.exportUserReportPdf();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/users/csv")
    public ResponseEntity<String> exportUsersCsv() {
        String csv = reportExportService.exportUserReportCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users-report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/sessions/pdf")
    public ResponseEntity<byte[]> exportSessionsPdf() {
        byte[] pdf = reportExportService.exportSessionReportPdf();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sessions-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/sessions/csv")
    public ResponseEntity<String> exportSessionsCsv() {
        String csv = reportExportService.exportSessionReportCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sessions-report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
