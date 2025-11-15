package com.kiosko;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class Dataset {
    private static final String CSV_HEADER = "id,nombre,codigo_barras,precio,cantidad,categoria";

    private final List<Product> products;

    private Dataset(List<Product> products) {
        this.products = new ArrayList<>(products);
    }

    public static Dataset fromCsv(Path csvPath) {
        List<Product> items = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
            // skip header
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] columns = line.split(",", -1);
                if (columns.length < 6) {
                    continue;
                }
                int id = Integer.parseInt(columns[0].trim());
                String name = columns[1].trim();
                String barcode = columns[2].trim();
                double price = Double.parseDouble(columns[3].trim());
                int quantity = Integer.parseInt(columns[4].trim());
                String category = columns[5].trim();

                items.add(new Product(id, name, barcode, price, quantity, category));
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar el dataset desde " + csvPath, e);
        }
        return new Dataset(items);
    }

    public List<Product> products() {
        return Collections.unmodifiableList(products);
    }

    public synchronized boolean addProduct(Product product) {
        Objects.requireNonNull(product, "product");
        boolean exists = products.stream().anyMatch(p -> p.id() == product.id());
        if (exists) {
            return false;
        }
        products.add(product);
        return true;
    }

    public Optional<Product> findProductById(int productId) {
        return products.stream().filter(product -> product.id() == productId).findFirst();
    }

    public void removeProducts(List<Product> soldProducts) {
        Objects.requireNonNull(soldProducts, "soldProducts");
        Map<Integer, Long> counts = soldProducts
            .stream()
            .collect(Collectors.groupingBy(Product::id, Collectors.counting()));
        for (Map.Entry<Integer, Long> entry : counts.entrySet()) {
            int productId = entry.getKey();
            long quantitySold = entry.getValue();
            int index = indexOfProduct(productId);
            if (index == -1) {
                throw new IllegalArgumentException("Producto " + productId + " no existe en inventario.");
            }
            Product current = products.get(index);
            if (quantitySold > current.quantity()) {
                throw new IllegalArgumentException(
                    "Stock insuficiente para " + current.name() + ". Vendido: " + quantitySold + ", disponible: " + current.quantity()
                );
            }
            int remaining = (int) (current.quantity() - quantitySold);
            if (remaining == 0) {
                products.remove(index);
            } else {
                products.set(
                    index,
                    new Product(current.id(), current.name(), current.barcode(), current.price(), remaining, current.category())
                );
            }
        }
    }

    public void writeProducts(Path output, List<Product> data) {
        Objects.requireNonNull(output, "output");
        Objects.requireNonNull(data, "data");
        try (BufferedWriter writer = Files.newBufferedWriter(output)) {
            writer.write(CSV_HEADER);
            writer.newLine();
            for (Product product : data) {
                writer.write(
                    product.id()
                        + ","
                        + product.name()
                        + ","
                        + product.barcode()
                        + ","
                        + product.price()
                        + ","
                        + product.quantity()
                        + ","
                        + product.category()
                );
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo escribir el archivo " + output, e);
        }
    }

    private int indexOfProduct(int productId) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).id() == productId) {
                return i;
            }
        }
        return -1;
    }
}
