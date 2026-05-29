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

        if (result && purchase.ticket() != null && purchase.ticket().ticketType() == TicketType.SINGLE_USE) {
            purchase = new PurchaseData(
                    purchase.id(),
                    purchase.passenger(),
                    purchase.ticket(),
                    purchase.boughtAt(),
                    purchase.punchedAt(),
                    purchase.punchedIn(),
                    command.checkedAt()
            );
            purchaseCommandRepository.save(purchase);
        }

        ValidationData validation = new ValidationData(
                null,
                inspector,
                purchase,
                command.checkedAt(),
                command.checkedIn(),
                result
        );
        validationCommandRepository.save(validation);

        return new ValidationResultView(result);
    }
}
