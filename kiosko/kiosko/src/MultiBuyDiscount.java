public class MultiBuyDiscount extends QuantityDiscount {
    public MultiBuyDiscount(String name, int buyQuantity, int payQuantity) {
        super(name, buyQuantity, computePercentage(buyQuantity, payQuantity));
    }

    private static double computePercentage(int buy, int pay) {
        if (buy <= 0 || pay <= 0 || pay > buy) {
            throw new IllegalArgumentException("Valores invalidos para buy/pay.");
        }
        return (double) (buy - pay) / buy;
    }
}

