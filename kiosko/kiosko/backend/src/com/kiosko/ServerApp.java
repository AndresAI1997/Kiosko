package com.kiosko;

import com.kiosko.discounts.Discounts;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ServerApp {
    private static final int PORT = 8080;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static Dataset dataset;
    private static Path csvPath;
    private static ActionSystem actionSystem;
    private static List<Discounts> scenarioDiscounts;
    private static final List<DiscountRecord> customDiscounts = new java.util.concurrent.CopyOnWriteArrayList<>();
    private static final List<SaleRecord> customSales = new java.util.concurrent.CopyOnWriteArrayList<>();

    public static void main(String[] args) throws IOException {
        csvPath = Paths.get("productos_kiosko.csv");
        SaleScenario scenario = SaleScenario.load(csvPath);
        dataset = scenario.dataset();
        actionSystem = new ActionSystem();
        scenarioDiscounts = scenario.discounts();
        scenarioDiscounts.forEach(actionSystem::addDiscount);

        Sale sampleSale = new Sale(scenario.productsForSale(), scenario.defaultDiscount());
        actionSystem.addSale(sampleSale);

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/inventario", exchange ->
            handleGet(exchange, () -> productsToJson(scenario.dataset().products()))
        );
        server.createContext("/api/descuentos", ServerApp::handleDiscounts);
        server.createContext("/api/ventas", ServerApp::handleSales);
        server.createContext("/api/productos", ServerApp::handleProducts);

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Servidor iniciado en http://localhost:" + PORT);
    }

    private static void handleGet(HttpExchange exchange, JsonSupplier supplier) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendString(exchange, 405, "{\"error\":\"Metodo no permitido\"}");
            return;
        }
        try {
            String json = supplier.get();
            sendString(exchange, 200, json);
        } catch (Exception ex) {
            ex.printStackTrace();
            sendString(exchange, 500, "{\"error\":\"Error interno\"}");
        }
    }

    private static void sendString(HttpExchange exchange, int status, String body) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "application/json; charset=utf-8");
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type");
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void handleProducts(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendString(exchange, 204, "");
            return;
        }
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            ProductPayload payload = ProductPayload.fromForm(body);
            if (payload == null) {
                sendString(exchange, 400, "{\"error\":\"Datos invalidos\"}");
                return;
            }
            Product product = payload.toProduct();
            boolean added = dataset.addProduct(product);
            if (!added) {
                sendString(exchange, 409, "{\"error\":\"ID duplicado\"}");
                return;
            }
            dataset.writeProducts(csvPath, new java.util.ArrayList<>(dataset.products()));
            sendString(exchange, 201, "{\"status\":\"ok\"}");
            return;
        }
        sendString(exchange, 405, "{\"error\":\"Metodo no permitido\"}");
    }

    private static void handleDiscounts(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        if ("OPTIONS".equals(method)) {
            sendString(exchange, 204, "");
            return;
        }
        if ("GET".equals(method)) {
            sendString(exchange, 200, discountsToJson());
            return;
        }
        if ("POST".equals(method)) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            DiscountRecord record = DiscountRecord.fromForm(body);
            if (record == null) {
                sendString(exchange, 400, "{\"error\":\"Datos invalidos\"}");
                return;
            }
            customDiscounts.add(record);
            sendString(exchange, 201, "{\"status\":\"ok\"}");
            return;
        }
        sendString(exchange, 405, "{\"error\":\"Metodo no permitido\"}");
    }

    private static void handleSales(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        if ("OPTIONS".equals(method)) {
            sendString(exchange, 204, "");
            return;
        }
        if ("GET".equals(method)) {
            sendString(exchange, 200, salesToJson());
            return;
        }
        if ("POST".equals(method)) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            SaleRecord record = SaleRecord.fromForm(body);
            if (record == null) {
                sendString(exchange, 400, "{\"error\":\"Datos invalidos\"}");
                return;
            }
            customSales.add(record);
            sendString(exchange, 201, "{\"status\":\"ok\"}");
            return;
        }
        sendString(exchange, 405, "{\"error\":\"Metodo no permitido\"}");
    }

    private static String productsToJson(List<Product> products) {
        return products
            .stream()
            .map(product ->
                "{"
                    + "\"id\":" + product.id()
                    + ",\"name\":\"" + jsonEscape(product.name()) + "\""
                    + ",\"barcode\":\"" + jsonEscape(product.barcode()) + "\""
                    + ",\"price\":" + product.price()
                    + ",\"quantity\":" + product.quantity()
                    + ",\"category\":\"" + jsonEscape(product.category()) + "\""
                    + "}"
            )
            .collect(Collectors.joining(",", "[", "]"));
    }

    private static String discountsToJson() {
        List<String> responses = scenarioDiscounts
            .stream()
            .map(discount ->
                "{"
                    + "\"name\":\"" + jsonEscape(discount.name()) + "\""
                    + ",\"type\":\"" + jsonEscape(discount.getClass().getSimpleName()) + "\""
                    + ",\"condition\":\"" + jsonEscape("Automatico") + "\""
                    + ",\"percentage\":0"
                    + "}"
            )
            .collect(Collectors.toList());

        customDiscounts
            .stream()
            .map(record ->
                "{"
                    + "\"name\":\"" + jsonEscape(record.name) + "\""
                    + ",\"type\":\"" + jsonEscape(record.type) + "\""
                    + ",\"condition\":\"" + jsonEscape(record.condition) + "\""
                    + ",\"percentage\":" + record.percentage
                    + "}"
            )
            .forEach(responses::add);

        return responses.stream().collect(Collectors.joining(",", "[", "]"));
    }

    private static String salesToJson() {
        List<String> base = actionSystem
            .getSales()
            .stream()
            .map(sale ->
                "{"
                    + "\"id\":" + sale.hashCode()
                    + ",\"createdAt\":\"" + sale.createdAt().format(DATE_TIME_FORMATTER) + "\""
                    + ",\"totalBeforeDiscount\":" + sale.totalBeforeDiscount()
                    + ",\"totalAfterDiscount\":" + sale.cost()
                    + ",\"products\":" + productsToJson(sale.products())
                    + "}"
            )
            .collect(Collectors.toList());

        customSales
            .stream()
            .map(record ->
                "{"
                    + "\"id\":" + record.id
                    + ",\"createdAt\":\"" + jsonEscape(record.date) + "\""
                    + ",\"totalBeforeDiscount\":" + record.total
                    + ",\"totalAfterDiscount\":" + record.total
                    + ",\"products\":[{\"id\":0,\"name\":\"" + jsonEscape(record.product) + "\",\"barcode\":\"\",\"price\":" + record.total + ",\"quantity\":" + record.quantity + ",\"category\":\"\"}]"
                    + "}"
            )
            .forEach(base::add);

        return base.stream().collect(Collectors.joining(",", "[", "]"));
    }

    private static String jsonEscape(String input) {
        return input.replace("\"", "\\\"");
    }

    private interface JsonSupplier {
        String get();
    }

    private static final class ProductPayload {
        private final int id;
        private final String name;
        private final String barcode;
        private final double price;
        private final int quantity;
        private final String category;

        private ProductPayload(int id, String name, String barcode, double price, int quantity, String category) {
            this.id = id;
            this.name = name;
            this.barcode = barcode;
            this.price = price;
            this.quantity = quantity;
            this.category = category;
        }

        public static ProductPayload fromForm(String body) {
            String[] pairs = body.split("&");
            java.util.Map<String, String> values = new java.util.HashMap<>();
            for (String pair : pairs) {
                if (pair.isBlank()) {
                    continue;
                }
                String[] kv = pair.split("=", 2);
                String key = java.net.URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String value = kv.length > 1 ? java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
                values.put(key, value);
            }
            try {
                int id = Integer.parseInt(values.getOrDefault("id", "").trim());
                double price = Double.parseDouble(values.getOrDefault("price", "0").trim());
                int quantity = Integer.parseInt(values.getOrDefault("quantity", "0").trim());
                String name = values.getOrDefault("name", "").trim();
                String barcode = values.getOrDefault("barcode", "").trim();
                String category = values.getOrDefault("category", "").trim();
                if (name.isEmpty() || barcode.isEmpty() || category.isEmpty()) {
                    return null;
                }
                return new ProductPayload(id, name, barcode, price, quantity, category);
            } catch (NumberFormatException ex) {
                return null;
            }
        }

        public Product toProduct() {
            return new Product(id, name, barcode, price, quantity, category);
        }
    }

    private static final class DiscountRecord {
        final String name;
        final String type;
        final String condition;
        final double percentage;

        private DiscountRecord(String name, String type, String condition, double percentage) {
            this.name = name;
            this.type = type;
            this.condition = condition;
            this.percentage = percentage;
        }

        static DiscountRecord fromForm(String body) {
            String[] pairs = body.split("&");
            java.util.Map<String, String> values = new java.util.HashMap<>();
            for (String pair : pairs) {
                if (pair.isBlank()) continue;
                String[] kv = pair.split("=", 2);
                String key = java.net.URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String value = kv.length > 1 ? java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
                values.put(key, value);
            }
            String name = values.getOrDefault("name", "").trim();
            String type = values.getOrDefault("type", "").trim();
            String condition = values.getOrDefault("condition", "").trim();
            double percentage;
            try {
                percentage = Double.parseDouble(values.getOrDefault("percentage", "0").trim());
            } catch (NumberFormatException ex) {
                return null;
            }
            if (name.isEmpty() || type.isEmpty()) {
                return null;
            }
            return new DiscountRecord(name, type, condition, percentage);
        }
    }

    private static final class SaleRecord {
        final long id = System.nanoTime();
        final String product;
        final int quantity;
        final double total;
        final String date;

        private SaleRecord(String product, int quantity, double total, String date) {
            this.product = product;
            this.quantity = quantity;
            this.total = total;
            this.date = date;
        }

        static SaleRecord fromForm(String body) {
            java.util.Map<String, String> values = new java.util.HashMap<>();
            for (String pair : body.split("&")) {
                if (pair.isBlank()) continue;
                String[] kv = pair.split("=", 2);
                String key = java.net.URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String value = kv.length > 1 ? java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
                values.put(key, value);
            }
            String product = values.getOrDefault("product", "").trim();
            String date = values.getOrDefault("date", java.time.LocalDate.now().toString());
            try {
                int quantity = Integer.parseInt(values.getOrDefault("quantity", "0").trim());
                double total = Double.parseDouble(values.getOrDefault("total", "0").trim());
                if (product.isEmpty() || quantity <= 0 || total <= 0) {
                    return null;
                }
                return new SaleRecord(product, quantity, total, date);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }
}
