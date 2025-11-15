package com.kiosko.discounts;

public class PercentageDiscount extends QuantityDiscount {
    public PercentageDiscount(String name, double percentage) {
        super(name, 1, percentage);
    }
}
