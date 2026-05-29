package org.example.eticket.data.repositories.validation;

import org.example.eticket.data.dto.ValidationData;

public interface ValidationCommandRepository {
    ValidationData save(ValidationData validation);
}

