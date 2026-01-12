package com.lockin.client.model;

public class Compartment {
    private Long id;
    private int position;
    private String size;
    private Long customerId;
    private Long reservationId;

    public Compartment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }

    @Override
    public String toString() {
        return "Box " + position + " (" + size + ")";
    }
}