package com.julian.notificator.service.impl.mail;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.julian.notificator.model.MessageRequest;
import com.julian.notificator.model.telegram.DestinationTelegramType;
import com.julian.notificator.service.AbstractNotificationService;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Service
public class MailServiceImpl extends AbstractNotificationService {

    private static final String MAIL = "Mail";

    @Value("${mail.to}")
    private String to;

    @Value("${mail.subject}")
    private String subject;

    private final JavaMailSender mailSender;

    @Override
    public void sendMessage(String message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            
            String html = buildHtmlMessage(message);
            helper.setText(html, true); 

            mailSender.send(mimeMessage);
            log.debug("MailService - sendMessage HTML: {}", message);

        } catch (Exception e) {
            log.error("❌ Error enviando mensaje a Mail: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void sendMessageFile(MessageRequest messageRequest, DestinationTelegramType destination) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);

            String html = buildHtmlMessage(messageRequest.getMessage());
            helper.setText(html, true);

            if (messageRequest.getMessagePayload().getFile() != null && !messageRequest.getMessagePayload().getFile().isBlank()) {
                byte[] fileBytes = Base64.getDecoder().decode(messageRequest.getMessagePayload().getFile());

                ByteArrayResource resource = new ByteArrayResource(fileBytes) {
                    @Override
                    public String getFilename() {
                        return messageRequest.getMessagePayload().getFilename();
                    }
                };

                helper.addAttachment(messageRequest.getMessagePayload().getFilename(), resource);
            }

            mailSender.send(mimeMessage);

            log.info("MailService - sendMessageFile ✅ Email enviado con adjunto: {}",
                    messageRequest.getMessagePayload().getFilename());

        } catch (Exception e) {
            log.error("❌ Error enviando mensaje con adjunto a Mail: {}", e.getMessage(), e);
        }
    }

    private String buildHtmlMessage(String message) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
            </head>
            <body style="margin:0; padding:0; background-color:#f6f7f9;">
                <div style="max-width:600px; margin:40px auto; background:#fafafa;
                            border-radius:10px; padding:26px;
                            font-family:Arial, Helvetica, sans-serif;
                            box-shadow:0 4px 12px rgba(0,0,0,0.06);">
                    
                    <h2 style="color:#2f3a45; margin-top:0;">
                        📬 Notificación
                    </h2>

                    <p style="color:#444; font-size:15px; line-height:1.7;">
                        %s
                    </p>

                    <hr style="border:none; border-top:1px solid #e6e6e6; margin:32px 0;">

                    <p style="font-size:12px; color:#8a8a8a; text-align:center;">
                        Mensaje enviado automáticamente · Notificator
                    </p>
                </div>
            </body>
            </html>
            """.formatted(message.replace("\n", "<br>"));
    }

    @Override
    public String getChannelName() {
        return MAIL;
    }

}
