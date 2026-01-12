package com.lockin.server.controllers;

import com.lockin.server.entities.Operator;
import com.lockin.server.repositories.OperatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operators")
@CrossOrigin(origins = "*")
public class OperatorController {

    @Autowired
    private OperatorRepository operatorRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Operator loginRequest) {
        // --- DEBUG PRINTS ---
        System.out.println("🔔 LOGIN ATTEMPT RECEIVED");
        System.out.println("User: " + loginRequest.getUsername());
        System.out.println("Pass: " + loginRequest.getPassword());
        // --------------------

        Operator op = operatorRepository.findByUsername(loginRequest.getUsername()).orElse(null);

        if (op != null && op.getPassword().equals(loginRequest.getPassword())) {
            System.out.println("✅ Login Success!");
            return ResponseEntity.ok(op);
        }

        System.out.println("❌ Login Failed - Invalid Credentials or User Not Found");
        return ResponseEntity.status(401).body("Invalid credentials");
    }
}