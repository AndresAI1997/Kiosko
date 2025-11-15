package com.kiosko.filters;

import com.kiosko.Product;
import java.util.List;

public class QuantityFilter implements DiscountFilter {
    private final int minItems;

    public QuantityFilter(int minItems) {
        if (minItems <= 0) {
            throw new IllegalArgumentException("El minimo debe ser positivo.");
        }
        this.minItems = minItems;
    }

    @Override
    public boolean matches(List<Product> products) {
        return products.size() >= minItems;
    }
}
