package com.healthsync.healthsync.controller;

import com.healthsync.healthsync.service.QRCodeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class QRCodeController {

    private final QRCodeService qrCodeService;

    @Value("${app.base-url}")
    private String baseUrl;

    public QRCodeController(QRCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    @GetMapping(value = "/qr/{publicId}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQRCode(@PathVariable String publicId) {
        try {

            String url = baseUrl + "/emergency/card/" + publicId;

            byte[] qr = qrCodeService.generateQRCode(url, 250, 250);

            return ResponseEntity
                    .ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(qr);

        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .build();
        }
    }
}