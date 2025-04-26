package com.almetpt.coursework.bookclub.service;

import com.almetpt.coursework.bookclub.model.EventApplication;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class QRCodeService {

    public String generateQRCode(EventApplication application) {
        try {
            String qrData = String.format(
                    "Event: %s\nUser: %s\nApplication ID: %d",
                    application.getEvent().getTitle(),
                    application.getUser().getEmail(),
                    application.getId()
            );

            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.MARGIN, 2);

            BitMatrix matrix = new MultiFormatWriter().encode(
                    qrData,
                    BarcodeFormat.QR_CODE,
                    300,
                    300,
                    hints
            );

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", output);

            return Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (Exception e) {
            log.error("Error generating QR code", e);
            throw new RuntimeException("Error generating QR code", e);
        }

    }
}
