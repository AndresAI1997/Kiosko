package com.kiosko;

import com.kiosko.discounts.Discounts;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActionSystem {
    private final List<Object> actions = new ArrayList<>();

    public void addSale(Sale sale) {
        if (sale == null) {
            throw new IllegalArgumentException("La venta no puede ser null.");
        }
        actions.add(sale);
    }

    public void addDiscount(Discounts discount) {
        if (discount == null) {
            throw new IllegalArgumentException("El descuento no puede ser null.");
        }
        actions.add(discount);
    }

    public List<Object> getActions() {
        return Collections.unmodifiableList(actions);
    }

    public List<Sale> getSales() {
        return actions
            .stream()
            .filter(entry -> entry instanceof Sale)
            .map(entry -> (Sale) entry)
            .toList();
    }

    public List<Discounts> getDiscounts() {
        return actions
            .stream()
            .filter(entry -> entry instanceof Discounts)
            .map(entry -> (Discounts) entry)
            .toList();
    }
}
