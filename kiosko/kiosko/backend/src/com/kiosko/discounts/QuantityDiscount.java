package com.kiosko.discounts;

import com.kiosko.Product;
import com.kiosko.filters.QuantityFilter;
import java.util.List;

public class QuantityDiscount extends Discounts {
    private final QuantityFilter quantityFilter;
    private final double percentage;

    public QuantityDiscount(String name, int minItems, double percentage) {
        super(name);
        if (percentage <= 0 || percentage >= 1) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 1 (excluyendo extremos).");
        }
        this.quantityFilter = new QuantityFilter(minItems);
        this.percentage = percentage;
    }

    @Override
    public boolean cumple(List<Product> products) {
        return quantityFilter.matches(products);
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
