package org.example.eticket.data.repositories;

import org.example.eticket.data.entities.Validation;
import org.springframework.stereotype.Repository;

@Repository
public class ValidationCommandRepositoryAdapter implements ValidationCommandRepository {

    private final ValidationJpaRepository validationJpaRepository;

    public ValidationCommandRepositoryAdapter(ValidationJpaRepository validationJpaRepository) {
        this.validationJpaRepository = validationJpaRepository;
    }

    @Override
    public Validation save(Validation validation) {
        return validationJpaRepository.save(validation);
    }
}

