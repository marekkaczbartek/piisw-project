package org.example.eticket.application.service.validation;

import lombok.RequiredArgsConstructor;
import org.example.eticket.application.exception.NotFoundException;
import org.example.eticket.application.model.validation.ValidateTicketCommand;
import org.example.eticket.application.model.validation.ValidationResultView;
import org.example.eticket.application.service.auth.UserResolver;
import org.example.eticket.data.entities.Purchase;
import org.example.eticket.data.entities.User;
import org.example.eticket.data.entities.Validation;
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
        Purchase purchase = purchaseQueryRepository.findById(command.purchaseId())
                .orElseThrow(() -> new NotFoundException("Purchase not found"));
        User inspector = userResolver.resolveByEmail(inspectorEmail, "Inspector not found");
        boolean result = isValidForInspection(
                purchase,
                command.checkedAt(),
                command.checkedIn()
        );

        if (result && purchase.getTicket() != null && purchase.getTicket().getTicketType() == TicketType.SINGLE_USE) {
            purchase.setExpiresAt(command.checkedAt());
            purchaseCommandRepository.save(purchase);
        }

        Validation validation = Validation.builder()
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
