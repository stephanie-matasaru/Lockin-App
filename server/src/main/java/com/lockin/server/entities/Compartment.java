package com.lockin.server.entities;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Compartment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int position;
    private String size;

    @ManyToOne
    @JoinColumn(name = "locker_id")
    @JsonIgnore
    private Locker locker;

    private Long customerId;
    private Long reservationId;

    public Compartment() {}

    public Compartment(int position, String size, Locker locker) {
        this.position = position;
        this.size = size;
        this.locker = locker;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public Locker getLocker() { return locker; }
    public void setLocker(Locker locker) { this.locker = locker; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }
}