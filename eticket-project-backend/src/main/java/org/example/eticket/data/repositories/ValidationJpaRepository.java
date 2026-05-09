package org.example.eticket.data.repositories;

import org.example.eticket.data.entities.Validation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ValidationJpaRepository extends JpaRepository<Validation, UUID> {
}

