package com.example.demo.controller;

import com.example.demo.service.ClerkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BackfillController {

    @Autowired
    private ClerkService clerkService;

    @PostMapping("/backfill")
    public String triggerBackfill() {
        return clerkService.syncAllUsers();
    }
}
