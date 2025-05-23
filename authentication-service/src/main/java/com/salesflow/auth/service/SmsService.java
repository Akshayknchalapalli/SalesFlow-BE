package com.salesflow.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SmsService {

    @Value("${twilio.account-sid:}")
    private String twilioAccountSid;
    
    @Value("${twilio.auth-token:}")
    private String twilioAuthToken;
    
    @Value("${twilio.phone-number:}")
    private String twilioPhoneNumber;
    
    @Value("${twilio.whatsapp-number:}")
    private String twilioWhatsappNumber;
    
    /**
     * Initializes the Twilio client if credentials are provided
     */
    private void initTwilio() {
        if (twilioAccountSid.isEmpty() || twilioAuthToken.isEmpty()) {
            log.warn("Twilio credentials not configured. SMS functionality is disabled.");
            return;
        }
        
        Twilio.init(twilioAccountSid, twilioAuthToken);
    }
    
    /**
     * Sends an SMS message to the specified phone number
     * 
     * @param to The recipient's phone number (E.164 format)
     * @param message The message to send
     * @return The message SID if sent successfully, null otherwise
     */
    public String sendSms(String to, String message) {
        try {
            // Initialize Twilio if not already initialized
            initTwilio();
            
            if (twilioAccountSid.isEmpty() || twilioPhoneNumber.isEmpty()) {
                log.warn("SMS sending skipped: Twilio not properly configured");
                return null;
            }
            
            // Format phone number to E.164 format if needed
            String formattedNumber = formatPhoneNumber(to);
            
            // Send the SMS
            Message twilioMessage = Message.creator(
                new PhoneNumber(formattedNumber),
                new PhoneNumber(twilioPhoneNumber),
                message
            ).create();
            
            log.info("SMS sent successfully to {}, SID: {}", formattedNumber, twilioMessage.getSid());
            return twilioMessage.getSid();
            
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send SMS", e);
        }
    }
    
    /**
     * Sends a WhatsApp message to the specified phone number
     * 
     * @param to The recipient's phone number (E.164 format)
     * @param message The message to send
     * @return The message SID if sent successfully, null otherwise
     */
    public String sendWhatsappMessage(String to, String message) {
        try {
            // Initialize Twilio if not already initialized
            initTwilio();
            
            if (twilioAccountSid.isEmpty() || twilioWhatsappNumber.isEmpty()) {
                log.warn("WhatsApp message sending skipped: Twilio not properly configured");
                return null;
            }
            
            // Format phone number to E.164 format if needed
            String formattedNumber = formatPhoneNumber(to);
            
            // Format WhatsApp numbers with "whatsapp:" prefix
            String whatsappFrom = "whatsapp:" + twilioWhatsappNumber;
            String whatsappTo = "whatsapp:" + formattedNumber;
            
            // Send the WhatsApp message
            Message twilioMessage = Message.creator(
                new PhoneNumber(whatsappTo),
                new PhoneNumber(whatsappFrom),
                message
            ).create();
            
            log.info("WhatsApp message sent successfully to {}, SID: {}", whatsappTo, twilioMessage.getSid());
            return twilioMessage.getSid();
            
        } catch (Exception e) {
            log.error("Failed to send WhatsApp message to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send WhatsApp message", e);
        }
    }
    
    /**
     * Formats a phone number to E.164 format if needed
     * 
     * @param phoneNumber The phone number to format
     * @return The formatted phone number
     */
    private String formatPhoneNumber(String phoneNumber) {
        // Basic formatting - ensure number starts with +
        if (!phoneNumber.startsWith("+")) {
            return "+" + phoneNumber;
        }
        return phoneNumber;
    }
}