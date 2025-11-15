package com.kiosko;

import com.kiosko.discounts.Discounts;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class App {
    private static final String CSV_FILE = "productos_kiosko.csv";

    public static void main(String[] args) {
        Path csvPath = Paths.get(CSV_FILE);
        SaleScenario scenario = SaleScenario.load(csvPath);
        Dataset dataset = scenario.dataset();

        List<Product> originalInventory = new ArrayList<>(dataset.products());
        Path originalSnapshot = Paths.get("inventario_original.csv");
        Path appliedSnapshot = Paths.get("inventario_post_venta.csv");
        dataset.writeProducts(originalSnapshot, originalInventory);

        ActionSystem actionSystem = new ActionSystem();
        scenario.discounts().forEach(actionSystem::addDiscount);

        List<Product> productosVenta = scenario.productsForSale();
        Discounts descuentoCategoria = scenario.defaultDiscount();

        Sale sale = new Sale(productosVenta, descuentoCategoria);
        actionSystem.addSale(sale);
        double totalSin = sale.totalBeforeDiscount();
        int totalCon = sale.cost();
        double descuentoNeto = totalSin - totalCon;
        double factor = totalSin == 0 ? 0 : totalCon / totalSin;

        printProductTable(productosVenta, factor);
        printDiscountTable(scenario.discounts(), productosVenta);

        System.out.println();
        System.out.printf("Total sin descuento: $%.2f%n", totalSin);
        System.out.printf("Total con descuento: $%d%n", totalCon);
        System.out.printf("Descuento neto aplicado: $%.2f%n", descuentoNeto);

        boolean ventaConfirmada = promptConfirmation();

        if (ventaConfirmada) {
            dataset.removeProducts(sale.products());
            dataset.writeProducts(appliedSnapshot, dataset.products());
        }
    }

    private static boolean promptConfirmation() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("Confirmar venta? (Y/N): ");
                String input = scanner.nextLine().trim().toUpperCase();
                if ("Y".equals(input)) {
                    return true;
                }
                if ("N".equals(input)) {
                    return false;
                }
                System.out.println("Respuesta invalida. Ingresa Y para si o N para no.");
            }
        }
    }

    private static void printProductTable(List<Product> products, double discountFactor) {
        System.out.println("Tabla de productos");
        System.out.printf("%-10s %-35s %-20s %-20s%n", "Cantidad", "Nombre", "Sin descuento", "Con descuento");
        for (Product product : products) {
            double sin = product.price();
            double con = sin * discountFactor;
            System.out.printf(
                "%-10d %-35s $%-19.2f $%-19.2f%n",
                1,
                product.name(),
                sin,
                con
            );
        }
    }

    private static void printDiscountTable(List<Discounts> discounts, List<Product> products) {
        System.out.println();
        System.out.println("Tabla de descuentos");
        System.out.printf("%-35s %-10s %-20s%n", "Nombre", "Aplica", "Total con descuento");
        for (Discounts discount : discounts) {
            boolean applies = discount.cumple(products);
            int cost = discount.costo(products);
            System.out.printf(
                "%-35s %-10s $%-19d%n",
                discount.name(),
                applies ? "Si" : "No",
                cost
            );
        }
    }
}
