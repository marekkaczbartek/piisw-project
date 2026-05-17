package org.example.eticket.data.repositories;

import org.example.eticket.data.entities.Validation;

public interface ValidationCommandRepository {
    Validation save(Validation validation);
}

