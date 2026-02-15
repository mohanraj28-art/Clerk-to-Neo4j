package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookController.class)
public class WebhookControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UserRepository userRepository;

  @MockitoBean
  private WebhookVerifier webhookVerifier;

  @Test
  @SuppressWarnings("null")
  public void testHandleClerkWebhook_UserCreated() throws Exception {
    String payload = """
        {
          "data": {
            "id": "user_123",
            "email_addresses": [{"email_address": "test@example.com"}],
            "first_name": "Test",
            "last_name": "User",
            "image_url": "http://example.com/avatar.png"
          },
          "type": "user.created"
        }
        """;

    mockMvc.perform(post("/api/webhooks/clerk")
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .header("svix-id", "test_id")
        .header("svix-timestamp", "test_timestamp")
        .header("svix-signature", "test_signature")
        .content(payload))
        .andExpect(status().isOk())
        .andExpect(content().string("User synced to Neo4j"));

    verify(webhookVerifier).verify(any(), any(), any());
    verify(userRepository).save(any(User.class));
  }
}
