package com.salesflow.activity.client;

import com.salesflow.contact.core.dto.ContactDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContactCoreClient {
    private final RestTemplate restTemplate;

    @Value("${contact.core.service.url}")
    private String contactCoreServiceUrl;

    @Value("${contact.core.service.timeout}")
    private int timeout;

    @CircuitBreaker(name = "contactCoreService", fallbackMethod = "getContactFallback")
    @Retry(name = "contactCoreService")
    public ContactDTO getContact(Long contactId) {
        log.debug("Fetching contact with ID: {}", contactId);
        return restTemplate.getForObject(
            contactCoreServiceUrl + "/contacts/{id}",
            ContactDTO.class,
            contactId
        );
    }

    @CircuitBreaker(name = "contactCoreService", fallbackMethod = "updateContactStageFallback")
    @Retry(name = "contactCoreService")
    public void updateContactStage(Long contactId, String stage) {
        log.debug("Updating stage to {} for contact ID: {}", stage, contactId);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(stage, headers);
        
        restTemplate.exchange(
            contactCoreServiceUrl + "/contacts/{id}/stage",
            HttpMethod.PUT,
            entity,
            Void.class,
            contactId
        );
    }

    @CircuitBreaker(name = "contactCoreService", fallbackMethod = "validateContactFallback")
    @Retry(name = "contactCoreService")
    public boolean validateContact(Long contactId) {
        try {
            log.debug("Validating contact with ID: {}", contactId);
            ContactDTO contact = getContact(contactId);
            return contact != null;
        } catch (Exception e) {
            log.error("Error validating contact with ID: {}", contactId, e);
            return false;
        }
    }

    // Fallback methods
    private ContactDTO getContactFallback(Long contactId, Exception e) {
        log.error("Fallback: Error fetching contact with ID: {}", contactId, e);
        return null;
    }

    private void updateContactStageFallback(Long contactId, String stage, Exception e) {
        log.error("Fallback: Error updating stage to {} for contact ID: {}", stage, contactId, e);
        throw new RuntimeException("Failed to update contact stage", e);
    }

    private boolean validateContactFallback(Long contactId, Exception e) {
        log.error("Fallback: Error validating contact with ID: {}", contactId, e);
        return false;
    }
} 