package com.kiosko.discounts;

import com.kiosko.Product;
import com.kiosko.filters.NameFilter;
import java.util.List;

public class NameDiscount extends Discounts {
    private final NameFilter nameFilter;
    private final double percentage;

    public NameDiscount(String name, String productName, int minOccurrences, double percentage) {
        super(name);
        if (percentage <= 0 || percentage >= 1) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 1 (excluyendo extremos).");
        }
        this.nameFilter = new NameFilter(productName, minOccurrences);
        this.percentage = percentage;
    }

    @Override
    public boolean cumple(List<Product> products) {
        return nameFilter.matches(products);
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
