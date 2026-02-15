package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
public class ClerkService {

    private static final Logger log = LoggerFactory.getLogger(ClerkService.class);
    private static final String CLERK_API_URL = "https://api.clerk.com/v1/users?limit=100";

    @Autowired
    private UserRepository userRepository;

    @Value("${clerk.secretKey}")
    private String clerkSecretKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String syncAllUsers() {
        if (clerkSecretKey == null || clerkSecretKey.contains("replace_with")) {
            throw new IllegalStateException(
                    "Clerk Secret Key is not configured. Please set 'clerk.secretKey' in application.properties.");
        }

        int totalSynced = 0;
        int offset = 0;
        boolean hasMore = true;

        try {
            while (hasMore) {
                String url = CLERK_API_URL + "&offset=" + offset;
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Authorization", "Bearer " + clerkSecretKey)
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    log.error("Failed to fetch users from Clerk. Status: {}, Body: {}", response.statusCode(),
                            response.body());
                    throw new RuntimeException("Failed to fetch users from Clerk: " + response.statusCode());
                }

                ClerkUser[] users = objectMapper.readValue(response.body(), ClerkUser[].class);

                if (users.length == 0) {
                    hasMore = false;
                } else {
                    for (ClerkUser clerkUser : users) {
                        saveUser(clerkUser);
                        totalSynced++;
                    }
                    offset += users.length;
                    log.info("Synced batch of {} users. Total so far: {}", users.length, totalSynced);
                }
            }
        } catch (Exception e) {
            log.error("Error during backfill", e);
            return "Backfill failed: " + e.getMessage();
        }

        return "Successfully synced " + totalSynced + " users.";
    }

    private void saveUser(ClerkUser data) {
        User user = new User();
        user.setClerkId(data.id);
        user.setFirstName(data.first_name);
        user.setLastName(data.last_name);
        user.setImageUrl(data.image_url);
        user.setCreatedAt(data.created_at);
        user.setUpdatedAt(data.updated_at);
        user.setLastSignInAt(data.last_sign_in_at);

        if (data.email_addresses != null && !data.email_addresses.isEmpty()) {
            user.setEmail(data.email_addresses.get(0).email_address);
        }

        userRepository.save(user);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ClerkUser {
        public String id;
        public String first_name;
        public String last_name;
        public String image_url;
        public Long created_at;
        public Long updated_at;
        public Long last_sign_in_at;
        public List<EmailAddress> email_addresses;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class EmailAddress {
        public String email_address;
    }
}
