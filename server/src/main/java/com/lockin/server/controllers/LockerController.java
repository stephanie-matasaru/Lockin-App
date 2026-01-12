package com.lockin.server.controllers;

import com.lockin.server.entities.Locker;
import com.lockin.server.entities.Compartment;
import com.lockin.server.repositories.LockerRepository;
import com.lockin.server.repositories.CompartmentRepository;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class LockerController {

    private final LockerRepository lockerRepository;
    private final CompartmentRepository compartmentRepository;

    public LockerController(LockerRepository lockerRepository, CompartmentRepository compartmentRepository) {
        this.lockerRepository = lockerRepository;
        this.compartmentRepository = compartmentRepository;
    }

    @GetMapping("/api/lockers")
    public List<Locker> getAllLockers() {
        return lockerRepository.findAll();
    }

    @PostMapping("/api/lockers/reserve")
    public Map<String, String> reserveCompartment(@RequestParam Long compartmentId) {
        Map<String, String> response = new HashMap<>();

        Optional<Compartment> compOpt = compartmentRepository.findById(compartmentId);

        if (compOpt.isPresent()) {
            Compartment comp = compOpt.get();

            if (comp.getReservationId() != null) {
                response.put("status", "fail");
                response.put("message", "Compartment is already taken!");
                return response;
            }

            comp.setReservationId(System.currentTimeMillis());
            compartmentRepository.save(comp);

            response.put("status", "success");
            response.put("message", "Reservation confirmed for " + comp.getSize() + " compartment.");
        } else {
            response.put("status", "error");
            response.put("message", "Compartment not found.");
        }

        return response;
    }
}