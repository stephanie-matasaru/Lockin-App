package com.lockin.server.entities;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Locker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String location;

    @OneToMany(mappedBy = "locker", cascade = CascadeType.ALL)
    private List<Compartment> compartments;

    public Locker() {}

    public Locker(String location) {
        this.location = location;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public List<Compartment> getCompartments() { return compartments; }
    public void setCompartments(List<Compartment> compartments) { this.compartments = compartments; }
}