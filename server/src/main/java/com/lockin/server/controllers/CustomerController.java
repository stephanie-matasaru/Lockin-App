package com.lockin.server.controllers;

import com.lockin.server.entities.Customer;
import com.lockin.server.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    // 1. REGISTER
    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String name,
                           @RequestParam String phone,
                           @RequestParam String email) {

        if (customerRepository.findByUsername(username).isPresent()) {
            return "error: username taken";
        }

        Customer c = new Customer();
        c.setUsername(username);
        c.setPassword(password);
        c.setName(name);
        c.setPhoneNumber(phone);
        c.setEmail(email);

        customerRepository.save(c);
        return "success";
    }

    // 2. GET ALL FOR ADMIN
    @GetMapping("/all")
    public String getAllCustomers() {
        List<Customer> list = customerRepository.findAll();
        StringBuilder sb = new StringBuilder();

        for (Customer c : list) {
            sb.append(c.getId()).append("|")
                    .append(c.getUsername()).append("|")
                    .append(c.getName()).append("|") // Ensure your Entity uses getName() or getFullName()
                    .append(c.getEmail()).append("|")
                    .append(c.getPhoneNumber()).append("\n");
        }

        return sb.toString();
    }

    // 3. GET DETAILS (For Client Profile)
    @GetMapping("/details")
    public String getDetails(@RequestParam String username) {
        return customerRepository.findByUsername(username)
                .map(c -> c.getName() + "|" + c.getEmail() + "|" + c.getPhoneNumber())
                .orElse("Unknown|Unknown|---");
    }
}