import java.util.List;
import java.util.Objects;

public abstract class Discounts {
    private final String name;

    protected Discounts(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public String name() {
        return name;
    }

    public abstract boolean cumple(List<Product> products);

    public abstract int costo(List<Product> products);
}

