package com.example.demo.controller;

import com.svix.Webhook;
import org.springframework.stereotype.Component;
import java.net.http.HttpHeaders;

@Component
public class WebhookVerifier {

    public void verify(String payload, HttpHeaders headers, String secret) {
        try {
            Webhook webhook = new Webhook(secret);
            webhook.verify(payload, headers);
        } catch (Exception e) {
            throw new RuntimeException("Invalid webhook signature", e);
        }
    }
}
