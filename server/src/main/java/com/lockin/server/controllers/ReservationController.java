package com.lockin.server.controllers;

import com.lockin.server.entities.Customer;
import com.lockin.server.entities.Reservation;
import com.lockin.server.repositories.CustomerRepository;
import com.lockin.server.repositories.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private CustomerRepository customerRepository;

    // 1. CREATE
    @PostMapping("/create")
    public String createReservation(
            @RequestParam String location,
            @RequestParam String compartment,
            @RequestParam String size,
            @RequestParam double price,
            @RequestParam String username
    ) {
        Optional<Customer> customerOpt = customerRepository.findByUsername(username);
        if (customerOpt.isEmpty()) return "User Not Found";

        Reservation res = new Reservation();
        res.setCustomer(customerOpt.get());
        res.setLocation(location);
        res.setCompartment(compartment);
        res.setSize(size);
        res.setPrice(price);
        res.setStatus("ACTIVE");
        res.setStartTime(LocalDateTime.now());

        reservationRepository.save(res);
        return "Saved";
    }

    // 2. MY ACTIVE
    @GetMapping("/my-active")
    public String getMyActive(@RequestParam String username) {
        List<Reservation> all = reservationRepository.findAll();
        for (Reservation r : all) {
            if (r.getCustomer() != null &&
                    r.getCustomer().getUsername().equals(username) &&
                    "ACTIVE".equalsIgnoreCase(r.getStatus())) {
                return r.getLocation() + "|" + r.getCompartment() + "|" + r.getSize();
            }
        }
        return "";
    }

    // 3. TAKEN BOXES (Strict Check)
    @GetMapping("/taken")
    public List<String> getAllTakenBoxes(@RequestParam String location) {
        List<String> taken = new ArrayList<>();
        List<Reservation> all = reservationRepository.findAll();

        for (Reservation r : all) {
            // Only add if explicitly ACTIVE
            if ("ACTIVE".equalsIgnoreCase(r.getStatus()) &&
                    r.getLocation().equalsIgnoreCase(location)) {
                taken.add(r.getCompartment());
            }
        }
        return taken;
    }

    // 4. CANCEL (Fixes "Box stays blocked")
    @PostMapping("/cancel")
    public String cancelReservation(@RequestParam String location, @RequestParam String compartment) {
        List<Reservation> all = reservationRepository.findAll();

        for (Reservation r : all) {
            if ("ACTIVE".equalsIgnoreCase(r.getStatus()) &&
                    r.getLocation().trim().equalsIgnoreCase(location.trim()) &&
                    r.getCompartment().trim().equalsIgnoreCase(compartment.trim())) {

                r.setStatus("COMPLETED");
                reservationRepository.save(r); // Force save to DB
                return "Cancelled";
            }
        }
        return "Error: Not Found";
    }

    // 5. EXTEND
    @PostMapping("/extend")
    public String extendReservation(@RequestParam String location,
                                    @RequestParam String compartment,
                                    @RequestParam double extraAmount) {
        List<Reservation> all = reservationRepository.findAll();

        for (Reservation r : all) {
            if ("ACTIVE".equalsIgnoreCase(r.getStatus()) &&
                    r.getLocation().trim().equalsIgnoreCase(location.trim()) &&
                    r.getCompartment().trim().equalsIgnoreCase(compartment.trim())) {

                double newPrice = (r.getPrice() == null ? 0.0 : r.getPrice()) + extraAmount;
                r.setPrice(newPrice);
                reservationRepository.save(r);
                return "Extended";
            }
        }
        return "Error: Not Found";
    }

    // 6. ADMIN STATS
    @GetMapping("/stats")
    public String getAdminStats() {
        List<Reservation> all = reservationRepository.findAll();
        double revenue = 0;
        int activeCount = 0;

        for (Reservation r : all) {
            if (r.getPrice() != null) revenue += r.getPrice();
            if ("ACTIVE".equalsIgnoreCase(r.getStatus())) activeCount++;
        }
        return revenue + ":" + activeCount;
    }

    // 7. GET ALL
    @GetMapping("/all")
    public String getAllReservations() {
        List<Reservation> list = reservationRepository.findAll();
        StringBuilder sb = new StringBuilder();

        for (Reservation r : list) {
            double price = (r.getPrice() != null) ? r.getPrice() : 0.0;
            Long custId = (r.getCustomer() != null) ? r.getCustomer().getId() : 0L;

            sb.append(r.getId()).append("|")
                    .append(r.getLocation()).append("|")
                    .append(r.getCompartment()).append("|")
                    .append(r.getSize()).append("|")
                    .append(price).append("|")
                    .append(r.getStatus()).append("|")
                    .append(custId).append("\n"); // <--- Added this!
        }
        return sb.toString();
    }
}