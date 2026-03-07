package com.healthsync.healthsync.controller;

import com.healthsync.healthsync.entity.PatientCardEntity;
import com.healthsync.healthsync.repository.PatientCardRepository;
import com.healthsync.healthsync.service.QRCodeService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/card")
public class CardPdfController {

    private final PatientCardRepository patientCardRepository;
    private final QRCodeService qrCodeService;

    public CardPdfController(
            PatientCardRepository patientCardRepository,
            QRCodeService qrCodeService) {

        this.patientCardRepository = patientCardRepository;
        this.qrCodeService = qrCodeService;
    }

    @GetMapping("/pdf/{publicId}")
    public ResponseEntity<byte[]> generatePdf(@PathVariable String publicId) throws Exception {

        PatientCardEntity card =
                patientCardRepository.findByPublicId(publicId)
                        .orElseThrow();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Document document = new Document();
        PdfWriter.getInstance(document, out);

        document.open();

        document.add(new Paragraph("HealthSync Emergency Card"));
        document.add(new Paragraph("Name: " + card.getUser().getName()));
        document.add(new Paragraph("Blood: " + card.getBloodType()));
        document.add(new Paragraph("Condition: " + card.getCurrentDisease()));
        document.add(new Paragraph("Emergency Contact: " + card.getEmergencyContact()));

        byte[] qr =
                qrCodeService.generateQRCode(
                        "http://localhost:8082/emergency/" + publicId,
                        200,
                        200
                );

        Image qrImage = Image.getInstance(qr);
        document.add(qrImage);

        document.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=card.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(out.toByteArray());
    }
}