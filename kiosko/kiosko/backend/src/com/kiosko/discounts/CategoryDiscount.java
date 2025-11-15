package com.kiosko.discounts;

import com.kiosko.Product;
import com.kiosko.filters.CategoryFilter;
import java.util.List;

public class CategoryDiscount extends Discounts {
    private final CategoryFilter categoryFilter;
    private final double percentage;

    public CategoryDiscount(String name, String category, int minOccurrences, double percentage) {
        super(name);
        if (percentage <= 0 || percentage >= 1) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 1 (excluyendo extremos).");
        }
        this.categoryFilter = new CategoryFilter(category, minOccurrences);
        this.percentage = percentage;
    }

    @Override
    public boolean cumple(List<Product> products) {
        return categoryFilter.matches(products);
    }

    @Override
    public int costo(List<Product> products) {
        double total = products.stream().mapToDouble(Product::price).sum();
        if (cumple(products)) {
            total = total * (1 - percentage);
        }
        return (int) Math.round(total);
    }
}
