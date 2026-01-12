package com.lockin.server.entities;

import jakarta.persistence.*;

@Entity
public class Operator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username; // <--- NEW: For login
    private String password; // <--- NEW: For login
    private String name;     // Real name (e.g., "John Smith")
    private String phoneNumber;
    private String email;

    public Operator() {}

    public Operator(String username, String password, String name, String phoneNumber, String email) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; } // <--- NEW
    public void setUsername(String username) { this.username = username; } // <--- NEW

    public String getPassword() { return password; } // <--- NEW
    public void setPassword(String password) { this.password = password; } // <--- NEW

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}