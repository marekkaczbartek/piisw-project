package org.example.eticket.application.mapper.purchase;

import org.example.eticket.application.model.purchase.PunchTicketView;
import org.example.eticket.application.model.purchase.PurchaseHistoryView;
import org.example.eticket.application.model.purchase.PurchaseView;
import org.example.eticket.application.model.purchase.ValidPurchaseView;
import org.example.eticket.data.dto.PurchaseData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface PurchaseMapper {

    @Mappings({
            @Mapping(source = "ticket.ticketType", target = "ticketType"),
            @Mapping(source = "ticket.discountType", target = "discountType"),
            @Mapping(source = "ticket.price", target = "price"),
            @Mapping(source = "ticket.durationMinutes", target = "durationMinutes")
    })
    PurchaseView toPurchaseView(PurchaseData purchase);

    @Mappings({
            @Mapping(source = "ticket.ticketType", target = "ticketType"),
            @Mapping(source = "ticket.discountType", target = "discountType"),
            @Mapping(source = "ticket.price", target = "price"),
            @Mapping(source = "ticket.durationMinutes", target = "durationMinutes")
    })
    PunchTicketView toPunchTicketView(PurchaseData purchase);

    @Mappings({
            @Mapping(source = "ticket.ticketType", target = "ticketType"),
            @Mapping(source = "ticket.discountType", target = "discountType"),
            @Mapping(source = "ticket.price", target = "price"),
            @Mapping(source = "ticket.durationMinutes", target = "durationMinutes")
    })
    ValidPurchaseView toValidPurchaseView(PurchaseData purchase);

    @Mappings({
            @Mapping(source = "ticket.ticketType", target = "ticketType"),
            @Mapping(source = "ticket.discountType", target = "discountType"),
            @Mapping(source = "ticket.price", target = "price"),
            @Mapping(source = "ticket.durationMinutes", target = "durationMinutes")
    })
    PurchaseHistoryView toPurchaseHistoryView(PurchaseData purchase);
}

