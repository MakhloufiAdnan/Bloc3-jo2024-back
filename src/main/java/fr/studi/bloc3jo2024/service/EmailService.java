package fr.studi.bloc3jo2024.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void envoyerEmailAvecQrCode(String destinataire, String sujet, String contenu, byte[] qrCodeImage, String nomFichierQrCode) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(destinataire);
            helper.setSubject(sujet);
            helper.setText(contenu, true); // true indique que le contenu est HTML

            // Convertir le byte[] en ByteArrayResource
            ByteArrayResource resource = new ByteArrayResource(qrCodeImage);

            // Ajouter le QR code en pièce jointe en utilisant la ByteArrayResource
            helper.addAttachment(nomFichierQrCode, resource);

            javaMailSender.send(message);
            logger.info("E-mail avec QR code envoyé à : {}", destinataire);

        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de l'e-mail à : {}", destinataire, e);
        }
    }
}