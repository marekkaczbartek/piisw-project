package org.example.eticket.api.mapper.purchase;

import org.example.eticket.api.dto.purchase.PunchTicketResponse;
import org.example.eticket.api.dto.purchase.PurchaseHistoryResponse;
import org.example.eticket.api.dto.purchase.PurchaseResponse;
import org.example.eticket.api.dto.purchase.ValidTicketResponse;
import org.example.eticket.application.model.purchase.PunchTicketView;
import org.example.eticket.application.model.purchase.PurchaseHistoryView;
import org.example.eticket.application.model.purchase.PurchaseView;
import org.example.eticket.application.model.purchase.ValidPurchaseView;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PurchaseResponseMapper {

    PurchaseResponse toPurchaseResponse(PurchaseView view);

    ValidTicketResponse toValidTicketResponse(ValidPurchaseView view);

    PurchaseHistoryResponse toPurchaseHistoryResponse(PurchaseHistoryView view);

    PunchTicketResponse toPunchTicketResponse(PunchTicketView view);
}

