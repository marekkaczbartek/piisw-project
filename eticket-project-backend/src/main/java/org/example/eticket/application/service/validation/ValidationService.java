package org.example.eticket.application.service.validation;

import lombok.RequiredArgsConstructor;
import org.example.eticket.application.exception.PurchaseNotFoundException;
import org.example.eticket.application.model.validation.ValidateTicketCommand;
import org.example.eticket.application.model.validation.ValidationResultView;
import org.example.eticket.application.service.auth.UserResolver;
import org.example.eticket.data.dto.PurchaseData;
import org.example.eticket.data.dto.UserData;
import org.example.eticket.data.dto.ValidationData;
import org.example.eticket.data.enums.TicketType;
import org.example.eticket.data.repositories.purchase.PurchaseCommandRepository;
import org.example.eticket.data.repositories.purchase.PurchaseQueryRepository;
import org.example.eticket.data.repositories.validation.ValidationCommandRepository;
import org.springframework.stereotype.Service;

import static org.example.eticket.application.service.validation.PurchaseValidator.isValidForInspection;

// TODO refactor

@Service
@RequiredArgsConstructor
public class ValidationService {

    private final PurchaseQueryRepository purchaseQueryRepository;
    private final PurchaseCommandRepository purchaseCommandRepository;
    private final ValidationCommandRepository validationCommandRepository;
    private final UserResolver userResolver;

    public ValidationResultView validatePurchase(ValidateTicketCommand command, String inspectorEmail) {
        PurchaseData purchase = purchaseQueryRepository.findById(command.purchaseId())
                .orElseThrow(PurchaseNotFoundException::new);
        UserData inspector = userResolver.resolveByEmail(inspectorEmail, "Inspector not found");
        boolean result = isValidForInspection(
                purchase,
                command.checkedAt(),
                command.checkedIn()
        );

        if (result && purchase.getTicket() != null && purchase.getTicket().getTicketType() == TicketType.SINGLE_USE) {
            purchase.setExpiresAt(command.checkedAt());
            purchaseCommandRepository.save(purchase);
        }

        ValidationData validation = ValidationData.builder()
                .inspector(inspector)
                .purchase(purchase)
                .checkedAt(command.checkedAt())
                .checkedIn(command.checkedIn())
                .result(result)
                .build();
        validationCommandRepository.save(validation);

        return new ValidationResultView(result);
    }
}
