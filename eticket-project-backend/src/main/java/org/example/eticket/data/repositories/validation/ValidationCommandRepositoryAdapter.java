package org.example.eticket.data.repositories.validation;

import org.example.eticket.data.dto.ValidationData;
import org.example.eticket.data.entities.Validation;
import org.example.eticket.data.mapper.ValidationDataMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ValidationCommandRepositoryAdapter implements ValidationCommandRepository {

    private final ValidationJpaRepository validationJpaRepository;
    private final ValidationDataMapper validationDataMapper;

    public ValidationCommandRepositoryAdapter(ValidationJpaRepository validationJpaRepository,
                                              ValidationDataMapper validationDataMapper) {
        this.validationJpaRepository = validationJpaRepository;
        this.validationDataMapper = validationDataMapper;
    }

    @Override
    public ValidationData save(ValidationData validation) {
        Validation saved = validationJpaRepository.save(validationDataMapper.toEntity(validation));
        return validationDataMapper.toData(saved);
    }
}

