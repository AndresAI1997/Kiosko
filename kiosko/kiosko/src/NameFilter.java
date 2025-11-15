import java.util.List;

public class NameFilter implements DiscountFilter {
    private final String name;
    private final int minOccurrences;

    public NameFilter(String name, int minOccurrences) {
        if (minOccurrences <= 0) {
            throw new IllegalArgumentException("El mínimo debe ser positivo.");
        }
        this.name = name;
        this.minOccurrences = minOccurrences;
    }

    @Override
    public boolean matches(List<Product> products) {
        return products
                .stream()
                .filter(product -> product.name().equalsIgnoreCase(name))
                .count() >= minOccurrences;
    }
}

