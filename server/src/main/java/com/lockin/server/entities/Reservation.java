package com.lockin.server.entities;

import com.lockin.server.entities.Customer;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- 1. RELATIONSHIP ---
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // --- 2. DATA FIELDS ---
    private String location;
    private String compartment;
    private String size;
    private Double price;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    public Reservation() {}

    // --- 3. GETTERS AND SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCompartment() { return compartment; }
    public void setCompartment(String compartment) { this.compartment = compartment; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}