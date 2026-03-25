package com.notificationservice.service;

import com.notificationservice.dto.UserEventDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Сервис для отправки email-уведомлений
 * Отправляет сообщения при создании или удалении аккаунта пользователя
 */
@Service
public class EmailService {
    
    @Value("${mail.from}")
    private String fromEmail;
    
    @Value("${mail.subject.create}")
    private String createSubject;
    
    @Value("${mail.subject.delete}")
    private String deleteSubject;
    
    @Value("${mail.text.create}")
    private String createText;
    
    @Value("${mail.text.delete}")
    private String deleteText;
    
    private final JavaMailSender mailSender;
    
    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    /**
     * Отправляет email на основе события о пользователе
     * @param event событие (CREATE или DELETE)
     * @param toEmail email получателя
     */
    public void sendEmail(UserEventDTO event, String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        
        if (event.getOperation() == UserEventDTO.OperationType.CREATE) {
            message.setSubject(createSubject);
            message.setText(String.format(createText, toEmail));
        } else {
            message.setSubject(deleteSubject);
            message.setText(String.format(deleteText, toEmail));
        }
        
        mailSender.send(message);
    }
}