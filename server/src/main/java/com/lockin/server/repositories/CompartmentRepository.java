package com.lockin.server.repositories;

import com.lockin.server.entities.Compartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.List;

@RepositoryRestResource(path = "compartments")
public interface CompartmentRepository extends JpaRepository<Compartment, Long> {
    List<Compartment> findByLockerId(Long lockerId);
}