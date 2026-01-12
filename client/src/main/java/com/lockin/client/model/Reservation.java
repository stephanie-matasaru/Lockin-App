package com.lockin.client.model;

public class Reservation {
    private Long id;
    private Long customerId;
    private Long lockerId;
    private String fullLocation;
    private String size;
    private Double estimatedPrice;
    private String status;

    public Reservation() {}

    // --- GETTERS  ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getLockerId() { return lockerId; }
    public void setLockerId(Long lockerId) { this.lockerId = lockerId; }

    public String getFullLocation() { return fullLocation; }
    public void setFullLocation(String fullLocation) { this.fullLocation = fullLocation; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public Double getEstimatedPrice() { return estimatedPrice; }
    public void setEstimatedPrice(Double estimatedPrice) { this.estimatedPrice = estimatedPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}