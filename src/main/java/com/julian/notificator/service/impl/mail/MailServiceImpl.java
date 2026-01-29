package com.julian.notificator.service.impl.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.julian.notificator.service.AbstractNotificationService;

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
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(to);
            mail.setSubject(subject);
            mail.setText(message);
            mailSender.send(mail);
            log.debug("MailService - sendMessage: {}", message);
        } catch (Exception e) {
            log.error("❌ Error enviando mensaje a Mail: {}", e.getMessage(), e);
        }
    }

    @Override
    public String getChannelName() {
        return "Mail";
    }

    
}
