package com.example.demo;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class Neo4jConnectionTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testNeo4jPersistence() {
        System.out.println("Starting Neo4j connectivity test...");

        User user = new User();
        user.setClerkId("test_conn_123");
        user.setFirstName("Connection");
        user.setLastName("Test");
        user.setEmail("test@connection.com");

        try {
            User savedUser = userRepository.save(user);
            System.out.println("Successfully saved user with ID: " + savedUser.getClerkId());

            boolean exists = userRepository.existsById("test_conn_123");
            System.out.println("User existence check: " + exists);

            assertTrue(exists, "User should exist in Neo4j after save");

            // Clean up
            userRepository.deleteById("test_conn_123");
            System.out.println("Cleaned up test user.");

        } catch (Exception e) {
            System.err.println("Failed to connect or save to Neo4j:");
            e.printStackTrace();
            throw e;
        }
    }
}
