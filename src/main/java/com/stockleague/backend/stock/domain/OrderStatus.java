package com.stockleague.backend.stock.domain;

public enum OrderStatus {
    WAITING,
    PARTIALLY_EXECUTED,
    EXECUTED,
    CANCELED,
    CANCELED_AFTER_PARTIAL,
    EXPIRED,
    FAILED
}
