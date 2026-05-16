package com.zayenha.qatra.donor.application;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;
import com.zayenha.qatra._shared.exception.NotFoundException;
import com.zayenha.qatra.donor.infrastructure.persistence.entity.DonationCertificateEntity;
import com.zayenha.qatra.donor.infrastructure.persistence.repository.DonationCertificateJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PdfCertificateService {

    private final DonationCertificateJpaRepository certificateRepository;

    public byte[] generateCertificate(Long certificateId) {
        var cert = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new NotFoundException("Certificate not found: " + certificateId, "CERTIFICATE_NOT_FOUND"));

        var baos = new ByteArrayOutputStream();
        var document = new Document();
        PdfWriter.getInstance(document, baos);
        document.open();

        var titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22);
        var bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 14);
        var labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

        document.add(new Paragraph("Certificat de Don du Sang", titleFont));
        document.add(new Paragraph(" "));

        document.add(new Paragraph("Nom du donneur :", labelFont));
        document.add(new Paragraph(cert.getDonorName(), bodyFont));
        document.add(new Paragraph(" "));

        document.add(new Paragraph("Date du don :", labelFont));
        document.add(new Paragraph(cert.getDonationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), bodyFont));
        document.add(new Paragraph(" "));

        document.add(new Paragraph("Centre :", labelFont));
        document.add(new Paragraph(cert.getCenterName(), bodyFont));
        document.add(new Paragraph(" "));

        if (cert.getMlCollected() != null) {
            document.add(new Paragraph("Quantité :", labelFont));
            document.add(new Paragraph(cert.getMlCollected() + " ml", bodyFont));
            document.add(new Paragraph(" "));
        }

        document.add(new Paragraph("Merci pour votre don qui sauve des vies !",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 14)));

        document.close();
        return baos.toByteArray();
    }
}
