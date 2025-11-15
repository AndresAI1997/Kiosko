import java.time.LocalDate;
import java.time.Month;
import java.util.List;

public class CleaningDecemberDiscount extends Discounts {
    private static final String CLEANING_CATEGORY = "Limpieza";
    private final CategoryFilter categoryFilter = new CategoryFilter(CLEANING_CATEGORY, 1);
    private final double percentage;

    public CleaningDecemberDiscount(double percentage) {
        super("Promo Limpieza Diciembre");
        if (percentage <= 0 || percentage >= 1) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 1 (excluyendo extremos).");
        }
        this.percentage = percentage;
    }

    @Override
    public boolean cumple(List<Product> products) {
        boolean isDecember = LocalDate.now().getMonth() == Month.DECEMBER;
        return isDecember && categoryFilter.matches(products);
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
