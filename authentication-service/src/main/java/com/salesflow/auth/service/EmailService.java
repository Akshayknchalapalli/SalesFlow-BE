package com.salesflow.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@salesflow.com}")
    private String fromEmail;
    
    /**
     * Sends a simple text email
     * 
     * @param to Recipient email address
     * @param subject Email subject
     * @param body Email body
     */
    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    /**
     * Sends an HTML email
     * 
     * @param to Recipient email address
     * @param subject Email subject
     * @param htmlBody Email body in HTML format
     */
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            
            mailSender.send(message);
            log.info("HTML email sent successfully to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }
    
    /**
     * Sends a password reset email with a token
     * 
     * @param to Recipient email address
     * @param username Username
     * @param resetToken Password reset token
     * @param expiryMinutes Token expiry time in minutes
     */
    @Async
    public void sendPasswordResetEmail(String to, String username, String resetToken, int expiryMinutes) {
        String subject = "Password Reset Instructions";
        String resetUrl = "https://yourapp.com/reset-password?token=" + resetToken;
        
        String htmlBody = String.format(
            "<html><body>" +
            "<h2>Password Reset</h2>" +
            "<p>Hello %s,</p>" +
            "<p>You have requested to reset your password. Please click the link below to reset your password:</p>" +
            "<p><a href='%s'>Reset Password</a></p>" +
            "<p>This link will expire in %d minutes.</p>" +
            "<p>If you did not request this, please ignore this email and your password will remain unchanged.</p>" +
            "<p>Best regards,<br/>SalesFlow Team</p>" +
            "</body></html>",
            username, resetUrl, expiryMinutes
        );
        
        sendHtmlEmail(to, subject, htmlBody);
    }
}