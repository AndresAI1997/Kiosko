package com.kiosko.filters;

import com.kiosko.Product;
import java.util.List;

public class CategoryFilter implements DiscountFilter {
    private final String category;
    private final int minOccurrences;

    public CategoryFilter(String category, int minOccurrences) {
        if (minOccurrences <= 0) {
            throw new IllegalArgumentException("El minimo debe ser positivo.");
        }
        this.category = category;
        this.minOccurrences = minOccurrences;
    }

    @Override
    public boolean matches(List<Product> products) {
        return products
                .stream()
                .filter(product -> product.category().equalsIgnoreCase(category))
                .count() >= minOccurrences;
    }
}
