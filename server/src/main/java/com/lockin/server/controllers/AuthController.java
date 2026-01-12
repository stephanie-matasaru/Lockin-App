package com.lockin.server.controllers;

import com.lockin.server.entities.Customer;
import com.lockin.server.entities.Operator;
import com.lockin.server.repositories.CustomerRepository;
import com.lockin.server.repositories.OperatorRepository;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class AuthController {

    private final CustomerRepository customerRepository;
    private final OperatorRepository operatorRepository;

    public AuthController(CustomerRepository customerRepository, OperatorRepository operatorRepository) {
        this.customerRepository = customerRepository;
        this.operatorRepository = operatorRepository;
    }

    @PostMapping("/api/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        System.out.println("Login Request: " + username + " / " + password); // Debug

        // 1. Check Admin
        Optional<Operator> operator = operatorRepository.findByUsername(username);
        if (operator.isPresent()) {
            if (operator.get().getPassword().equals(password)) {
                return "ADMIN";
            } else {
                return "WRONG_PASS";
            }
        }

        // 2. Check Client
        Optional<Customer> customer = customerRepository.findByUsername(username);
        if (customer.isPresent()) {
            if (customer.get().getPassword().equals(password)) {
                return "CLIENT";
            } else {
                return "WRONG_PASS";
            }
        }

        return "NOT_FOUND";
    }

    // Keep register exactly as it is
    @PostMapping("/api/register")
    public Map<String, String> register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String name,
            @RequestParam String phoneNumber,
            @RequestParam String email) {

        Map<String, String> response = new HashMap<>();
        if (customerRepository.findByUsername(username).isPresent()) {
            response.put("status", "fail");
            response.put("message", "Username already exists!");
            return response;
        }
        try {
            Customer newCustomer = new Customer(username, password, name, phoneNumber, email);
            customerRepository.save(newCustomer);
            response.put("status", "success");
            response.put("message", "Account created!");
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }
}