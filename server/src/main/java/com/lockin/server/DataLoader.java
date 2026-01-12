package com.lockin.server;

import com.lockin.server.entities.Customer;
import com.lockin.server.entities.Operator;
import com.lockin.server.entities.Locker;
import com.lockin.server.entities.Compartment;
import com.lockin.server.repositories.CustomerRepository;
import com.lockin.server.repositories.OperatorRepository;
import com.lockin.server.repositories.LockerRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final OperatorRepository operatorRepository;
    private final LockerRepository lockerRepository;

    public DataLoader(CustomerRepository customerRepository,
                      OperatorRepository operatorRepository,
                      LockerRepository lockerRepository) {
        this.customerRepository = customerRepository;
        this.operatorRepository = operatorRepository;
        this.lockerRepository = lockerRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (operatorRepository.count() == 0) {
            Operator admin = new Operator("admin", "password123", "Admin Name", "111-222", "admin@lockin.com");
            operatorRepository.save(admin);
            System.out.println("✅ OPERATOR CREATED");
        }

        if (lockerRepository.count() == 0) {

            createFullLocker("Central Station Hub", 12);  // 12 doors
            createFullLocker("Downtown Gym", 16);         // 16 doors
            createFullLocker("Mall Entrance", 8);         // 8 doors

            System.out.println("✅ REALISTIC LOCKERS CREATED (Total: 36 Compartments)");
        }
    }

    private void createFullLocker(String location, int count) {
        Locker locker = new Locker(location);
        List<Compartment> list = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            String size = (i <= 4) ? "Large" : "Small";

            list.add(new Compartment(i, size, locker));
        }

        locker.setCompartments(list);
        lockerRepository.save(locker);
    }
}