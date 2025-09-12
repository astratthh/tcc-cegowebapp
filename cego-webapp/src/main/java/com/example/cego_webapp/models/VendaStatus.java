package com.example.cego_webapp.models;

public enum VendaStatus {
    PENDENTE_PAGAMENTO, // A venda foi feita, mas aguarda o recebimento
    PAGA,               // O recebimento foi confirmado
    CANCELADA           // A venda foi cancelada
}