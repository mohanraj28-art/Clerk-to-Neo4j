package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JacksonReproductionTest {

    @Test
    public void testMapping() throws Exception {
        String json = """
                    {
                        "type": "user.created",
                        "data": {
                            "id": "user_123",
                            "first_name": "John",
                            "last_name": "Doe",
                            "image_url": "http://example.com/img.jpg",
                            "created_at": 1698765432000,
                            "updated_at": 1698765433000,
                            "last_sign_in_at": 1698765434000,
                            "email_addresses": [
                                { "email_address": "john@example.com" }
                            ]
                        }
                    }
                """;

        ObjectMapper mapper = new ObjectMapper();
        ClerkWebhookEvent event = mapper.readValue(json, ClerkWebhookEvent.class);

        assertNotNull(event);
        assertEquals("user.created", event.getType());
        assertNotNull(event.getData());
        assertEquals("user_123", event.getData().getId());
        assertEquals("John", event.getData().getFirst_name());
        assertEquals(1698765432000L, event.getData().getCreated_at());
        assertEquals(1698765433000L, event.getData().getUpdated_at());
        assertEquals(1698765434000L, event.getData().getLast_sign_in_at());

        List<EmailAddress> emails = event.getData().getEmail_addresses();
        assertNotNull(emails);
        assertEquals(1, emails.size());
        assertEquals("john@example.com", emails.get(0).getEmail_address());
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
