package com.salesflow.auth.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.salesflow.auth.domain.User;
import com.salesflow.auth.exception.InvalidOtpException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final Map<String, OtpData> otpStorage = new HashMap<>();
    private final EmailService emailService;
    private final SmsService smsService;
    
    @Value("${app.otp.length:6}")
    private int otpLength;
    
    @Value("${app.otp.expiry-minutes:5}")
    private int expiryMinutes;
    
    /**
     * Generates and sends an OTP to the user via the specified channel
     * 
     * @param user The user to send the OTP to
     * @param channel The channel to send the OTP through (email, sms, whatsapp)
     * @param phoneNumber The phone number to send the OTP to (for SMS and WhatsApp)
     * @return The generated OTP (for testing purposes)
     */
    public String generateAndSendOtp(User user, String channel, String phoneNumber) {
        String otp = generateOtp();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(expiryMinutes);
        
        // Store OTP with expiry time
        otpStorage.put(user.getUsername(), new OtpData(otp, expiryTime, 0));
        
        // Send OTP via the requested channel
        switch (channel.toLowerCase()) {
            case "email":
                sendOtpViaEmail(user, otp);
                break;
            case "sms":
                sendOtpViaSms(phoneNumber, otp);
                break;
            case "whatsapp":
                sendOtpViaWhatsapp(phoneNumber, otp);
                break;
            default:
                throw new IllegalArgumentException("Unsupported channel: " + channel);
        }
        
        return otp;
    }
    
    /**
     * Validates an OTP for a user
     * 
     * @param username The username of the user
     * @param otp The OTP to validate
     * @return true if the OTP is valid, false otherwise
     */
    public boolean validateOtp(String username, String otp) {
        OtpData otpData = otpStorage.get(username);
        
        if (otpData == null) {
            return false;
        }
        
        // Check if OTP has expired
        if (LocalDateTime.now().isAfter(otpData.expiryTime)) {
            otpStorage.remove(username);
            throw new InvalidOtpException("OTP has expired");
        }
        
        // Check if max attempts reached
        if (otpData.attempts >= 3) {
            otpStorage.remove(username);
            throw new InvalidOtpException("Maximum OTP validation attempts reached");
        }
        
        // Increment attempts
        otpData.attempts++;
        
        // Check if OTP matches
        if (!otpData.otp.equals(otp)) {
            throw new InvalidOtpException("Invalid OTP");
        }
        
        // OTP is valid, remove it
        otpStorage.remove(username);
        return true;
    }
    
    /**
     * Generates a random OTP of the configured length
     * 
     * @return The generated OTP
     */
    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        
        return otp.toString();
    }
    
    private void sendOtpViaEmail(User user, String otp) {
        String subject = "Your One-Time Password for SalesFlow";
        String body = String.format(
            "Hello %s,\n\nYour one-time password is: %s\n\nThis OTP will expire in %d minutes.\n\nBest regards,\nSalesFlow Team",
            user.getUsername(), otp, expiryMinutes
        );
        
        emailService.sendEmail(user.getEmail(), subject, body);
    }
    
    private void sendOtpViaSms(String phoneNumber, String otp) {
        // Implementation would use an SMS service like Twilio
        String message = String.format("Your SalesFlow OTP is: %s. Valid for %d minutes.", otp, expiryMinutes);
        smsService.sendSms(phoneNumber, message);
    }
    
    private void sendOtpViaWhatsapp(String phoneNumber, String otp) {
        // Implementation would use WhatsApp Business API or a service like Twilio
        String message = String.format("Your SalesFlow OTP is: %s. Valid for %d minutes.", otp, expiryMinutes);
        smsService.sendWhatsappMessage(phoneNumber, message);
    }
    
    /**
     * Inner class to store OTP data
     */
    private static class OtpData {
        private final String otp;
        private final LocalDateTime expiryTime;
        private int attempts;
        
        public OtpData(String otp, LocalDateTime expiryTime, int attempts) {
            this.otp = otp;
            this.expiryTime = expiryTime;
            this.attempts = attempts;
        }
    }
}