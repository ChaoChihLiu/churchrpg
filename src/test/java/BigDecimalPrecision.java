import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class BigDecimalPrecision {
    public static void main(String[] args) {
        BigDecimal num1 = new BigDecimal("123.456789");

        // Define MathContext with 5 significant digits and HALF_UP rounding
        MathContext mc = new MathContext(5, RoundingMode.HALF_UP);

        // Apply precision
        BigDecimal preciseNum = num1.round(mc);

        System.out.println("Number with controlled precision: " + preciseNum);
    }
}
