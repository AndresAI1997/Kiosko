package com.kiosko;

public record Product(
    int id,
    String name,
    String barcode,
    double price,
    int quantity,
    String category
) {}
