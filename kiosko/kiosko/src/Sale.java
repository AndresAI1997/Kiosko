import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Sale {
    private final List<Product> products;
    private final LocalDateTime createdAt;
    private final double totalBeforeDiscount;
    private final int cost;

    public Sale(List<Product> products, Discounts discounts) {
        Objects.requireNonNull(products, "products");
        if (products.isEmpty()) {
            throw new IllegalArgumentException("La venta debe contener al menos un producto.");
        }
        this.products = List.copyOf(products);
        this.createdAt = LocalDateTime.now();
        this.totalBeforeDiscount = products.stream().mapToDouble(Product::price).sum();
        this.cost = discounts == null ? (int) Math.round(totalBeforeDiscount) : discounts.costo(products);
    }

    public List<Product> products() {
        return Collections.unmodifiableList(products);
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    public double totalBeforeDiscount() {
        return totalBeforeDiscount;
    }

    public int cost() {
        return cost;
    }
}
