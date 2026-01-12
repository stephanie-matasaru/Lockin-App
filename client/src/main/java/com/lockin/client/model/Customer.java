package com.lockin.client.model;

public class Customer {
    private int id;
    private String username;
    private String password;
    private String fullName;
    private String phoneNumber;
    private String email;

    // Constructor used by ApiService
    public Customer(int id, String username, String password, String fullName, String phoneNumber, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    // --- GETTERS NEEDED FOR TABLE VIEW ---

    public int getId() { return id; }

    public String getUsername() { return username; }

    public String getFullName() { return fullName; } // Fixes "cannot resolve method"

    public String getPhoneNumber() { return phoneNumber; }

    public String getEmail() { return email; }
}