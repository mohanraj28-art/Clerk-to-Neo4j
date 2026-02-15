package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import java.util.List;
import org.springframework.http.HttpHeaders;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebhookController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebhookVerifier webhookVerifier;

    @Value("${clerk.webhook.secret}")
    private String webhookSecret;

    @GetMapping("/clerk")
    public String handleClerkWebhookHealthCheck() {
        return "Webhook endpoint is active. Please use POST to send data.";
    }

    @PostMapping("/clerk")
    public String handleClerkWebhook(@RequestBody String payload, @RequestHeader HttpHeaders springHeaders) {
        log.info("Received webhook request. Payload length: {}", payload.length());
        log.debug("Payload: {}", payload);

        // Log all headers for debugging
        springHeaders.forEach((key, value) -> log.info("Header '{}': {}", key, value));

        try {
            java.net.http.HttpHeaders svixHeaders = java.net.http.HttpHeaders.of(springHeaders, (k, v) -> true);
            webhookVerifier.verify(payload, svixHeaders, webhookSecret);
            log.info("Webhook signature verified successfully.");
        } catch (Exception e) {
            log.error("Invalid webhook signature", e);
            throw e;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        ClerkWebhookEvent event;
        try {
            event = objectMapper.readValue(payload, ClerkWebhookEvent.class);
            log.info("Successfully parsed webhook event. Type: {}", event.getType());
        } catch (JsonProcessingException e) {
            log.error("Failed to parse webhook payload", e);
            throw new RuntimeException("Failed to parse webhook payload", e);
        }

        if ("user.created".equals(event.getType()) || "user.updated".equals(event.getType())) {
            UserData data = event.getData();
            if (data == null) {
                log.error("Received event type {} but data is null", event.getType());
                return "Event processed but data was null";
            }
            log.info("Processing user data for Clerk ID: {}", data.getId());

            User user = new User();
            user.setClerkId(data.getId());
            user.setFirstName(data.getFirst_name());
            user.setLastName(data.getLast_name());
            user.setImageUrl(data.getImage_url());
            user.setCreatedAt(data.getCreated_at());
            user.setUpdatedAt(data.getUpdated_at());
            user.setLastSignInAt(data.getLast_sign_in_at());

            if (data.getEmail_addresses() != null && !data.getEmail_addresses().isEmpty()) {
                user.setEmail(data.getEmail_addresses().get(0).getEmail_address());
            } else {
                log.warn("No email addresses found for user {}", data.getId());
            }

            try {
                userRepository.save(user);
                log.info("Successfully saved user to Neo4j: {}", user.getClerkId());
                return "User synced to Neo4j";
            } catch (Exception e) {
                log.error("Failed to save user to Neo4j", e);
                throw new RuntimeException("Failed to save user to Neo4j", e);
            }
        }

        log.info("Ignored event type: {}", event.getType());
        return "Event ignored";
    }

    static class ClerkWebhookEvent {
        private String type;
        private UserData data;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public UserData getData() {
            return data;
        }

        public void setData(UserData data) {
            this.data = data;
        }
    }

    static class UserData {
        private String id;
        private String first_name;
        private String last_name;
        private String image_url;
        private Long created_at;
        private Long updated_at;
        private Long last_sign_in_at;
        private List<EmailAddress> email_addresses;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFirst_name() {
            return first_name;
        }

        public void setFirst_name(String first_name) {
            this.first_name = first_name;
        }

        public String getLast_name() {
            return last_name;
        }

        public void setLast_name(String last_name) {
            this.last_name = last_name;
        }

        public String getImage_url() {
            return image_url;
        }

        public void setImage_url(String image_url) {
            this.image_url = image_url;
        }

        public Long getCreated_at() {
            return created_at;
        }

        public void setCreated_at(Long created_at) {
            this.created_at = created_at;
        }

        public Long getUpdated_at() {
            return updated_at;
        }

        public void setUpdated_at(Long updated_at) {
            this.updated_at = updated_at;
        }

        public Long getLast_sign_in_at() {
            return last_sign_in_at;
        }

        public void setLast_sign_in_at(Long last_sign_in_at) {
            this.last_sign_in_at = last_sign_in_at;
        }

        public List<EmailAddress> getEmail_addresses() {
            return email_addresses;
        }

        public void setEmail_addresses(List<EmailAddress> email_addresses) {
            this.email_addresses = email_addresses;
        }
    }

    static class EmailAddress {
        private String email_address;

        public String getEmail_address() {
            return email_address;
        }

        public void setEmail_address(String email_address) {
            this.email_address = email_address;
        }
    }
}
