package fr.studi.bloc3jo2024.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import fr.studi.bloc3jo2024.exception.QrCodeGenerationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class QrCodeService {

    private static final Logger logger = LoggerFactory.getLogger(QrCodeService.class);

    /**
     * Génère un QR Code à partir du texte fourni.
     *
     * @param text   Le texte à encoder dans le QR Code.
     * @param width  La largeur du QR Code (en pixels).
     * @param height La hauteur du QR Code (en pixels).
     * @return Le QR Code sous forme de tableau de bytes (format PNG).
     * @throws QrCodeGenerationException si une erreur survient lors de la génération ou de la conversion.
     */
    public byte[] generateQRCode(String text, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            logger.info("QR Code généré avec succès pour le texte : {}", text);
            return outputStream.toByteArray();
        } catch (WriterException e) {
            logger.error("Erreur de génération (WriterException) du QR Code pour le texte : {}", text, e);
            throw new QrCodeGenerationException("Erreur lors de la création du QR code (format invalide)", e);
        } catch (IOException e) {
            logger.error("Erreur d'IO lors de la conversion du QR Code en image PNG pour le texte : {}", text, e);
            throw new QrCodeGenerationException("Erreur lors de la conversion du QR code en image", e);
        }
    }

    /**
     * Génère un QR Code avec des dimensions par défaut (200x200 pixels).
     *
     * @param text Le texte à encoder dans le QR Code.
     * @return Le QR Code sous forme de tableau de bytes (format PNG).
     */
    public byte[] generateQRCode(String text) {
        return generateQRCode(text, 200, 200); // Dimensions par défaut
    }
}
