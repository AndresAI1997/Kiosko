package com.kiosko.filters;

import com.kiosko.Product;
import java.util.List;

public interface DiscountFilter {
    boolean matches(List<Product> products);
}
