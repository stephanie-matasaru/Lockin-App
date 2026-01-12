package com.lockin.server.repositories;

import com.lockin.server.entities.Locker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "lockers")
public interface LockerRepository extends JpaRepository<Locker, Long> {
}