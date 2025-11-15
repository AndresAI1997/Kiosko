import java.util.List;

public interface DiscountFilter {
    boolean matches(List<Product> products);
}

