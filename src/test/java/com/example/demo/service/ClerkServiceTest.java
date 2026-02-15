package com.example.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClerkServiceTest {

    @Test
    public void testClerkUserMapping() throws Exception {
        String json = """
                    [
                        {
                            "id": "user_123",
                            "first_name": "Test",
                            "last_name": "User",
                            "image_url": "http://img.com",
                            "created_at": 1000,
                            "updated_at": 2000,
                            "last_sign_in_at": 3000,
                            "email_addresses": [
                                { "email_address": "test@test.com" }
                            ]
                        }
                    ]
                """;

        ObjectMapper mapper = new ObjectMapper();
        ClerkService.ClerkUser[] users = mapper.readValue(json, ClerkService.ClerkUser[].class);

        assertEquals(1, users.length);
        assertEquals("user_123", users[0].id);
        assertEquals("test@test.com", users[0].email_addresses.get(0).email_address);
        assertEquals(3000L, users[0].last_sign_in_at);
    }
}
