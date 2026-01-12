package com.lockin.server.repositories;

import com.lockin.server.entities.Operator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.Optional; // <--- Import this

@RepositoryRestResource(path = "operators")
public interface OperatorRepository extends JpaRepository<Operator, Long> {
    Optional<Operator> findByUsername(String username);
}