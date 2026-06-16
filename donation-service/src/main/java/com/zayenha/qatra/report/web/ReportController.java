package com.zayenha.qatra.report.web;

import com.zayenha.qatra.report.application.CenterReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class ReportController {
  private final CenterReportService service;

  @GetMapping("/api/v1/centers/{id}/report")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN')")
  public ResponseEntity<byte[]> getReport(@PathVariable Long id,
                                          @RequestParam LocalDate startDate,
                                          @RequestParam LocalDate endDate) {
    var csv = service.generateCenterReport(id, startDate, endDate);
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("text/csv"));
    headers.setContentDispositionFormData("attachment", "center-" + id + "-report.csv");
    return ResponseEntity.ok().headers(headers).body(csv.getBytes());
  }
}
