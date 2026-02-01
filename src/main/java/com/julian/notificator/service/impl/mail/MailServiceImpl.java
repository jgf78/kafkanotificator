package com.julian.notificator.service.impl.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.julian.notificator.service.AbstractNotificationService;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MailServiceImpl extends AbstractNotificationService {

    @Value("${mail.to}")
    private String to;

    @Value("${mail.subject}")
    private String subject;

    @Autowired
    private JavaMailSender mailSender;

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

    private String buildHtmlMessage(String message) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                </head>
                <body style="margin:0; padding:0; background-color:#f4f4f4;">
                    <div style="max-width:600px; margin:30px auto; background:#ffffff; border-radius:8px; padding:24px; font-family:Arial, sans-serif;">
                        <h2 style="color:#2c3e50; margin-top:0;">📬 Nueva notificación</h2>
                        <p style="color:#555; font-size:15px; line-height:1.6;">
                            %s
                        </p>
                        <hr style="border:none; border-top:1px solid #eee; margin:30px 0;">
                        <p style="font-size:12px; color:#999; text-align:center;">
                            Mensaje automático · Notificator
                        </p>
                    </div>
                </body>
                </html>
                """
                .formatted(message.replace("\n", "<br>"));
    }

    @Override
    public String getChannelName() {
        return "Mail";
    }

}
