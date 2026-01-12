package com.lockin.client.model;

import java.util.List;

public class Locker {
    private Long id;
    private String location;
    private List<Compartment> compartments;

    public Locker() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public List<Compartment> getCompartments() { return compartments; }
    public void setCompartments(List<Compartment> compartments) { this.compartments = compartments; }

    @Override
    public String toString() {
        return location;
    }
}