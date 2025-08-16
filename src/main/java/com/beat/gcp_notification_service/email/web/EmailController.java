package com.beat.gcp_notification_service.email.web;

import com.beat.gcp_notification_service.email.dto.EmailCommand;
import com.beat.gcp_notification_service.email.service.IEmailSender;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/notify")
public class EmailController {

    private final IEmailSender sender;

    public EmailController(IEmailSender sender) {
        this.sender = sender;
    }

    @PostMapping
    public ResponseEntity<?> send(@Valid @RequestBody EmailCommand cmd) {
        sender.sendEmailAPI(cmd);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}

