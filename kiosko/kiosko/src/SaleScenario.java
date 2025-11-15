import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SaleScenario {
    private final Dataset dataset;
    private final List<Product> productsForSale;
    private final List<Discounts> discounts;
    private final Discounts defaultDiscount;

    private SaleScenario(
        Dataset dataset,
        List<Product> productsForSale,
        List<Discounts> discounts,
        Discounts defaultDiscount
    ) {
        this.dataset = dataset;
        this.productsForSale = productsForSale;
        this.discounts = discounts;
        this.defaultDiscount = defaultDiscount;
    }

    public static SaleScenario load(Path csvPath) {
        Dataset dataset = Dataset.fromCsv(csvPath);
        List<Product> products = selectDemoProducts(dataset);

        Discounts categoria = new CategoryDiscount("Promo Golosinas 10%", "Golosinas", 2, 0.10);
        Discounts nombre = new NameDiscount("Promo Alfajor Especial", "Alfajor 1", 1, 0.05);
        Discounts limpieza = new CleaningDecemberDiscount(0.20);
        Discounts cantidad = new QuantityDiscount("Promo por cantidad 3x", 3, 0.08);
        Discounts buy3pay2 = new MultiBuyDiscount("Promo 3x2 general", 3, 2);
        List<Discounts> discounts = List.of(categoria, nombre, limpieza, cantidad, buy3pay2);

        return new SaleScenario(dataset, products, discounts, categoria);
    }

    private static List<Product> selectDemoProducts(Dataset dataset) {
        List<String> targetCategories = List.of("Golosinas", "Gaseosa", "Pastas", "Productos Congelados", "Limpieza");
        List<Product> selected = new ArrayList<>();

        for (String category : targetCategories) {
            dataset
                .products()
                .stream()
                .filter(product -> product.category().equalsIgnoreCase(category))
                .filter(product -> selected.stream().noneMatch(sel -> sel.id() == product.id()))
                .findFirst()
                .ifPresent(selected::add);
        }

        for (Product product : dataset.products()) {
            if (selected.size() >= 6) {
                break;
            }
            boolean already = selected.stream().anyMatch(sel -> sel.id() == product.id());
            if (!already) {
                selected.add(product);
            }
        }
        return selected;
    }

    public Dataset dataset() {
        return dataset;
    }

    public List<Product> productsForSale() {
        return productsForSale;
    }

    public List<Discounts> discounts() {
        return discounts;
    }

    public Discounts defaultDiscount() {
        return defaultDiscount;
    }
}
